package kr.kro.airbob.domain.accommodation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.CreateAccommodationDto;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import kr.kro.airbob.domain.accommodation.entity.Address;
import kr.kro.airbob.domain.accommodation.entity.Amenity;
import kr.kro.airbob.domain.accommodation.entity.OccupancyPolicy;
import kr.kro.airbob.domain.accommodation.repository.AccommodationAmenityRepository;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.accommodation.repository.AddressRepository;
import kr.kro.airbob.domain.accommodation.repository.AmenityRepository;
import kr.kro.airbob.domain.accommodation.repository.OccupancyPolicyRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final MemberRepository memberRepository;
    private final AmenityRepository amenityRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;
    private final OccupancyPolicyRepository occupancyPolicyRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public Long createAccommodation(CreateAccommodationDto request) {
        //todo 커스텀 예외로 만들기
        Member member = memberRepository.findById(request.getHostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        OccupancyPolicy occupancyPolicy = OccupancyPolicy.createOccupancyPolicy(request.getOccupancyPolicyInfo());
        occupancyPolicyRepository.save(occupancyPolicy);

        Address address = Address.createAddress(request.getAddressInfo());
        addressRepository.save(address);

        Accommodation accommodation = Accommodation.createAccommodation(request, address, occupancyPolicy, member);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        //사전에 정의해둔 어메니티만 저장 가능
        saveValidAmenities(request, savedAccommodation);

        return savedAccommodation.getId();
    }

    private void saveValidAmenities(CreateAccommodationDto request, Accommodation savedAccommodation) {
        Map<AmenityType, Integer> amenityCountMap = getAmenityCountMap(request);

        List<Amenity> amenities = amenityRepository.findByNameIn(amenityCountMap.keySet());

        saveAccommodationAmenity(savedAccommodation, amenities, amenityCountMap);
    }

    private void saveAccommodationAmenity(Accommodation savedAccommodation, List<Amenity> amenities,
                           Map<AmenityType, Integer> amenityCountMap) {
        for (Amenity amenity : amenities) {
            AmenityType type = AmenityType.valueOf(amenity.getName().toUpperCase());
            int count = amenityCountMap.get(type);

            AccommodationAmenity accommodationAmenity = AccommodationAmenity.createAccommodationAmenity(
                    savedAccommodation, amenity, count);
            accommodationAmenityRepository.save(accommodationAmenity);
        }
    }

    private Map<AmenityType, Integer> getAmenityCountMap(CreateAccommodationDto request) {
        return request.getAmenityInfos().stream()
                .filter(info -> AmenityType.isValid(info.getName()))
                .collect(Collectors.toMap(
                        info -> AmenityType.valueOf(info.getName().toUpperCase()),
                        AmenityInfo::getCount,
                        Integer::sum
                ));
    }
}
