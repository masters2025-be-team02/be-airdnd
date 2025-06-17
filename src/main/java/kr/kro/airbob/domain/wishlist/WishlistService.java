package kr.kro.airbob.domain.wishlist;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.kro.airbob.cursor.dto.CursorRequest;
import kr.kro.airbob.cursor.dto.CursorResponse;
import kr.kro.airbob.cursor.util.CursorPageInfoCreator;
import kr.kro.airbob.domain.accommodation.dto.AccommodationResponse;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.common.MemberRole;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.wishlist.dto.WishlistRequest;
import kr.kro.airbob.domain.wishlist.dto.WishlistResponse;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistAmenityProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistImageProjection;
import kr.kro.airbob.domain.wishlist.dto.projection.WishlistRatingProjection;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationAccessDeniedException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationDuplicateException;
import kr.kro.airbob.domain.wishlist.exception.WishlistAccommodationNotFoundException;
import kr.kro.airbob.domain.wishlist.exception.WishlistNotFoundException;
import kr.kro.airbob.domain.wishlist.repository.WishlistAccommodationRepository;
import kr.kro.airbob.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistService {

	private final MemberRepository memberRepository;
	private final WishlistRepository wishlistRepository;
	private final AccommodationRepository accommodationRepository;
	private final WishlistAccommodationRepository wishlistAccommodationRepository;

	private final CursorPageInfoCreator cursorPageInfoCreator;

	@Transactional
	public WishlistResponse.CreateResponse createWishlist(WishlistRequest.createRequest request, Long loggedInMemberId) {

		Member member = findMemberById(loggedInMemberId);
		log.info("{} 사용자 조회 성공", member.getId());

		Wishlist wishlist = Wishlist.builder()
			.name(request.name())
			.member(member)
			.build();

		Wishlist savedWishlist = wishlistRepository.save(wishlist);
		return new WishlistResponse.CreateResponse(savedWishlist.getId());
	}

	@Transactional
	public WishlistResponse.UpdateResponse updateWishlist(Long wishlistId, WishlistRequest.updateRequest request, Long loggedInMemberId) {

		Member member = findMemberById(loggedInMemberId); // todo: exist?
		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		validateWishlistOwnership(foundWishlist, loggedInMemberId);

		log.info("위시리스트 이름 {} -> {} 변경", foundWishlist.getName(), request.name());
		foundWishlist.updateName(request.name());

		return new WishlistResponse.UpdateResponse(foundWishlist.getId());
	}

	@Transactional
	public void deleteWishlist(Long wishlistId, Long loggedInMemberId) {
		// 위시리스트 존재, 작성자 id 검증을 위한 조회
		Wishlist foundWishlist = findWishlistById(wishlistId);
		log.info("{} 위시리스트 조회 성공", foundWishlist.getId());

		Member member = findMemberById(loggedInMemberId);

		validateWishlistOwnershipOrAdmin(foundWishlist, member);

		// 위시리스트에 속한 숙소 삭제
		wishlistAccommodationRepository.deleteAllByWishlistId(foundWishlist.getId());
		wishlistRepository.delete(foundWishlist);
	}

	@Transactional(readOnly = true)
	public WishlistResponse.WishlistInfos findWishlists(CursorRequest.CursorPageRequest request, Long loggedInMemberId) {

		// todo: exist?
		Member member = findMemberById(loggedInMemberId); // 사용자 존재 여부를 위해 넣었는데, 필요한지 의문. member는 사용하지 않음

		Long lastId = request.lastId();
		LocalDateTime lastCreatedAt = request.lastCreatedAt();

		Slice<Wishlist> wishlistSlice = wishlistRepository.findByMemberIdWithCursor(
			loggedInMemberId,
			lastId,
			lastCreatedAt,
			PageRequest.of(0, request.size())
		);
		log.info("위시리스크 목록 조회: {} 개, 다음 페이지 여부: {}", wishlistSlice.getContent().size(), wishlistSlice.hasNext());

		List<Long> wishlistIds = wishlistSlice.getContent().stream()
			.map(Wishlist::getId)
			.toList();

		// 위시리스트별 숙소 개수 조회
		Map<Long, Long> wishlistItemCounts = wishlistAccommodationRepository.countByWishlistIds(wishlistIds);

		// 위시리스트별 가장 최근에 추가된 숙소 썸네일 Url 조회
		Map<Long, String> thumbnailUrls = wishlistAccommodationRepository.findLatestThumbnailUrlsByWishlistIds(wishlistIds);

		List<WishlistResponse.WishlistInfo> wishlistInfos = wishlistSlice.getContent().stream()
			.map(wishlist ->
				new WishlistResponse.WishlistInfo(
					wishlist.getId(),
					wishlist.getName(),
					wishlist.getCreatedAt(),
					wishlistItemCounts.getOrDefault(wishlist.getId(), 0L),
					thumbnailUrls.get(wishlist.getId()) // nullable
				)).toList();

		CursorResponse.PageInfo pageInfo = cursorPageInfoCreator.createPageInfo(
			wishlistSlice.getContent(),
			wishlistSlice.hasNext(),
			Wishlist::getId,
			Wishlist::getCreatedAt
		);

		return new WishlistResponse.WishlistInfos(wishlistInfos, pageInfo);
	}

	@Transactional
	public WishlistResponse.CreateWishlistAccommodationResponse createWishlistAccommodation(Long wishlistId,
		WishlistRequest.CreateWishlistAccommodationRequest request, Long loggedInMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		Accommodation accommodation = findAccommodationById(request.accommodationId());
		Member member = findMemberById(loggedInMemberId); // todo: exist?

		validateWishlistOwnership(wishlist, member.getId());
		validateWishlistAccommodationDuplicate(wishlistId, accommodation.getId());

		WishlistAccommodation wishlistAccommodation = WishlistAccommodation.builder()
			.wishlist(wishlist)
			.accommodation(accommodation)
			.build();

		WishlistAccommodation savedWishlistAccommodation
			= wishlistAccommodationRepository.save(wishlistAccommodation);

		return new WishlistResponse.CreateWishlistAccommodationResponse(savedWishlistAccommodation.getId());
	}

	@Transactional
	public WishlistResponse.UpdateWishlistAccommodationResponse updateWishlistAccommodation(Long wishlistId,
		Long wishlistAccommodationId, WishlistRequest.UpdateWishlistAccommodationRequest request,
		Long loggedInMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		WishlistAccommodation wishlistAccommodation = findWishlistAccommodation(wishlistAccommodationId);

		Member member = findMemberById(loggedInMemberId); // todo: exist?

		validateWishlistOwnership(wishlist, member.getId());
		validateWishlistAccommodationOwnership(wishlistAccommodation, wishlistId);

		wishlistAccommodation.updateMemo(request.memo());

		return new WishlistResponse.UpdateWishlistAccommodationResponse(wishlistAccommodation.getId());
	}

	@Transactional
	public void deleteWishlistAccommodation(Long wishlistId, Long wishlistAccommodationId, Long loggedInMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		WishlistAccommodation wishlistAccommodation = findWishlistAccommodation(wishlistAccommodationId);

		Member member = findMemberById(loggedInMemberId); // todo: exist?

		validateWishlistOwnership(wishlist, member.getId());
		validateWishlistAccommodationOwnership(wishlistAccommodation, wishlistId);

		wishlistAccommodationRepository.delete(wishlistAccommodation);
	}

	@Transactional(readOnly = true)
	public WishlistResponse.WishlistAccommodationInfos findWishlistAccommodations(Long wishlistId,
		CursorRequest.CursorPageRequest request, Long loggedInMemberId) {

		Wishlist wishlist = findWishlistById(wishlistId);
		Member member = findMemberById(loggedInMemberId); // todo: exist?

		validateWishlistOwnership(wishlist, member.getId());

		Long lastId = request.lastId();
		LocalDateTime lastCreatedAt = request.lastCreatedAt();

		Slice<WishlistAccommodation> wishlistAccommodationSlice = wishlistAccommodationRepository.findByWishlistIdWithCursor(
			wishlistId,
			lastId,
			lastCreatedAt,
			PageRequest.of(0, request.size())
		);
		log.info("{} 위시리스크 항목 목록 조회: {} 개, 다음 페이지 여부: {}",
			wishlistId, wishlistAccommodationSlice.getContent().size(), wishlistAccommodationSlice.hasNext());

		List<WishlistAccommodation> wishlistAccommodations = wishlistAccommodationSlice.getContent();

		if (wishlistAccommodations.isEmpty()) {
			CursorResponse.PageInfo pageInfo = cursorPageInfoCreator.createPageInfo(
				wishlistAccommodations,
				wishlistAccommodationSlice.hasNext(),
				WishlistAccommodation::getId,
				WishlistAccommodation::getCreatedAt
			);
			return new WishlistResponse.WishlistAccommodationInfos(List.of(), pageInfo);
		}

		List<Long> wishlistAccommodationIds = wishlistAccommodations.stream()
			.map(WishlistAccommodation::getId)
			.toList();

		// 숙소 이미지
		Map<Long, List<String>> imageUrlsMap = getAccommodationImageUrls(wishlistAccommodationIds);

		// 숙소 편의시설
		Map<Long, List<AccommodationResponse.AmenityInfoResponse>> amenitiesMap = getAccommodationAmenities(wishlistAccommodationIds);

		// 숙소 리뷰 평점
		Map<Long, Double> ratingMap = getAccommodationRatings(wishlistAccommodationIds);

		List<WishlistResponse.WishlistAccommodationInfo> wishlistAccommodationInfos = wishlistAccommodations.stream()
			.map(wa -> {
				Accommodation accommodation = wa.getAccommodation();

				AccommodationResponse.WishlistAccommodationInfo accommodationInfo =
					new AccommodationResponse.WishlistAccommodationInfo(
						accommodation.getId(),
						accommodation.getName(),
						imageUrlsMap.getOrDefault(wa.getId(), List.of()),
						amenitiesMap.getOrDefault(wa.getId(), List.of()),
						ratingMap.get(wa.getId())
					);

				return new WishlistResponse.WishlistAccommodationInfo(
					wa.getId(),
					wa.getMemo(),
					accommodationInfo
				);
			}).toList();

		CursorResponse.PageInfo pageInfo = cursorPageInfoCreator.createPageInfo(
			wishlistAccommodations,
			wishlistAccommodationSlice.hasNext(),
			WishlistAccommodation::getId,
			WishlistAccommodation::getCreatedAt
		);

		return new WishlistResponse.WishlistAccommodationInfos(wishlistAccommodationInfos, pageInfo);
	}

	private Map<Long, List<String>> getAccommodationImageUrls(List<Long> wishlistAccommodationIds) {
		List<WishlistImageProjection> results = wishlistAccommodationRepository
			.findAccommodationImagesByWishlistAccommodationIds(wishlistAccommodationIds);

		return results .stream()
			.collect(Collectors.groupingBy(
				WishlistImageProjection::wishlistAccommodationId,
				Collectors.mapping(
					WishlistImageProjection::url,
					Collectors.toList()
				)
			));
	}

	private Map<Long, List<AccommodationResponse.AmenityInfoResponse>> getAccommodationAmenities(
		List<Long> wishlistAccommodationIds) {

		List<WishlistAmenityProjection> results
			= wishlistAccommodationRepository.findAccommodationAmenitiesByWishlistAccommodationIds(wishlistAccommodationIds);

		return results.stream()
			.collect(Collectors.groupingBy(
				WishlistAmenityProjection::wishlistAccommodationId,
				Collectors.mapping(
					result -> new AccommodationResponse.AmenityInfoResponse(
						result.type(),
						result.count()
					),
					Collectors.toList()
				)
			));
	}

	private Map<Long, Double> getAccommodationRatings(List<Long> wishlistAccommodationIds) {
		List<WishlistRatingProjection> results
			= wishlistAccommodationRepository.findAccommodationRatingsByWishlistAccommodationIds(wishlistAccommodationIds);

		return results.stream()
			.collect(Collectors.toMap(
				WishlistRatingProjection::wishlistAccommodationId,
				WishlistRatingProjection::averageRating
			));
	}

	private Wishlist findWishlistById(Long wishlistId) {
		return wishlistRepository.findById(wishlistId).orElseThrow(WishlistNotFoundException::new);
	}

	private Member findMemberById(Long loggedInMemberId) {
		return memberRepository.findById(loggedInMemberId).orElseThrow(MemberNotFoundException::new);
	}

	private Accommodation findAccommodationById(Long accommodationId) {
		return accommodationRepository.findById(accommodationId).orElseThrow(AccommodationNotFoundException::new);
	}

	private WishlistAccommodation findWishlistAccommodation(Long wishlistAccommodationId){
		return wishlistAccommodationRepository.findById(wishlistAccommodationId)
			.orElseThrow(WishlistAccommodationNotFoundException::new);
	}

	private void validateWishlistOwnership(Wishlist wishlist, Long memberId) {
		if (!wishlist.isOwnedBy(memberId)) {
			log.error("사용자 아이디: {}, 위시리스트 작성자 아이디: {}", memberId, wishlist.getMember().getId());
			throw new WishlistAccessDeniedException();
		}
	}

	private void validateWishlistOwnershipOrAdmin(Wishlist wishlist, Member member) {
		if (!wishlist.isOwnedBy(member.getId()) && member.getRole() != MemberRole.ADMIN) {
			log.error("사용자 아이디: {}, 위시리스트 작성자 아이디: {}", member.getId(), wishlist.getMember().getId());
			throw new WishlistAccessDeniedException();
		}
	}

	private void validateWishlistAccommodationOwnership(WishlistAccommodation wishlistAccommodation, Long wishlistId) {
		if (!wishlistAccommodation.isOwnedBy(wishlistId)) {
			log.error("위시리스트: {}, 항목이 속한 위시리스트: {}", wishlistId, wishlistAccommodation.getAccommodation().getId());
			throw new WishlistAccommodationAccessDeniedException();
		}
	}

	private void validateWishlistAccommodationDuplicate(Long wishlistId, Long accommodationId) {
		if (wishlistAccommodationRepository.existsByWishlistIdAndAccommodationId(wishlistId, accommodationId)) {
			throw new WishlistAccommodationDuplicateException();
		}
	}
}
