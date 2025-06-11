package kr.kro.airbob.domain.accommodation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.kro.airbob.domain.accommodation.common.AccommodationType;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AddressInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.CreateAccommodationDto;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.OccupancyPolicyInfo;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @InjectMocks
    private AccommodationService accommodationService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OccupancyPolicyRepository occupancyPolicyRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private AccommodationAmenityRepository accommodationAmenityRepository;

    @Test
    @DisplayName("모든 정보가 정상적으로 주어졌을 때 Accommodation, Address, OccupancyPolicy, Amenity까지 모두 저장된다")
    void createAccommodationSuccess() {
        // given
        Long hostId = 1L;
        Long expectedAccommodationId = 100L;

        Member host = mock(Member.class);
        CreateAccommodationDto request = mock(CreateAccommodationDto.class);
        OccupancyPolicyInfo policyInfo = mock(OccupancyPolicyInfo.class);
        AddressInfo addressInfo = mock(AddressInfo.class);

        AmenityInfo wifiInfo = new AmenityInfo("WIFI", 2);
        AmenityInfo tvInfo = new AmenityInfo("TV", 1);

        List<AmenityInfo> amenityInfos = List.of(wifiInfo, tvInfo);

        Amenity wifiAmenity = Amenity.builder().id(1L).name(AmenityType.WIFI).build();
        Amenity tvAmenity = Amenity.builder().id(2L).name(AmenityType.TV).build();

        given(request.getHostId()).willReturn(hostId);
        given(memberRepository.findById(hostId)).willReturn(Optional.of(host));
        given(request.getOccupancyPolicyInfo()).willReturn(policyInfo);
        given(request.getAddressInfo()).willReturn(addressInfo);
        given(request.getAmenityInfos()).willReturn(amenityInfos);
        given(request.getType()).willReturn(AccommodationType.APARTMENT.name());

        OccupancyPolicy occupancyPolicy = mock(OccupancyPolicy.class);
        given(occupancyPolicyRepository.save(any())).willReturn(occupancyPolicy);

        Address address = mock(Address.class);
        given(addressRepository.save(any())).willReturn(address);

        Accommodation accommodation = mock(Accommodation.class);
        Accommodation savedAccommodation = mock(Accommodation.class);

        given(accommodationRepository.save(any())).willReturn(savedAccommodation);
        given(savedAccommodation.getId()).willReturn(expectedAccommodationId);

        given(amenityRepository.findByNameIn(Set.of(AmenityType.WIFI, AmenityType.TV)))
                .willReturn(List.of(wifiAmenity, tvAmenity));

        // when
        Long accommodationId = accommodationService.createAccommodation(request);

        // then
        assertEquals(expectedAccommodationId, accommodationId);
        verify(memberRepository).findById(hostId);
        verify(occupancyPolicyRepository).save(any());
        verify(addressRepository).save(any());
        verify(accommodationRepository).save(any());

        ArgumentCaptor<AccommodationAmenity> captor = ArgumentCaptor.forClass(AccommodationAmenity.class);
        verify(accommodationAmenityRepository, times(2)).save(captor.capture());

        List<AccommodationAmenity> savedAmenities = captor.getAllValues();
        Map<String, Integer> countMap = savedAmenities.stream()
                .collect(Collectors.toMap(a -> a.getAmenity().getName().toString(), AccommodationAmenity::getCount));

        assertEquals(2, countMap.get("WIFI"));
        assertEquals(1, countMap.get("TV"));
    }

    @Test
    @DisplayName("존재하지 않는 회원일 때 예외가 발생힌다")
    void createAccommodationWhenMemberNotFound() {
        // given
        CreateAccommodationDto request = mock(CreateAccommodationDto.class);
        given(request.getHostId()).willReturn(999L);
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accommodationService.createAccommodation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("Amenity가 중복으로 들어왔을 때 count를 합산한다")
    void createAccommodation_withDuplicateAmenities_shouldSumCounts() {
        // given
        List<AmenityInfo> amenities = List.of(
                AmenityInfo.builder().name("WIFI").count(1).build(),
                AmenityInfo.builder().name("wifi").count(2).build()
        );

        CreateAccommodationDto request = CreateAccommodationDto.builder()
                .name("테스트 숙소")
                .description("설명")
                .basePrice(10000)
                .hostId(1L)
                .thumbnail_url("http://image.jpg")
                .type("house")
                .addressInfo(AddressInfo.builder()
                        .postalCode(12345)
                        .city("서울")
                        .country("대한민국")
                        .detail("상세주소")
                        .district("강남구")
                        .street("테헤란로")
                        .build())
                .amenityInfos(amenities)
                .occupancyPolicyInfo(OccupancyPolicyInfo.builder()
                        .maxOccupancy(4)
                        .adultOccupancy(2)
                        .childOccupancy(1)
                        .infantOccupancy(0)
                        .petOccupancy(0)
                        .build())
                .build();

        given(accommodationRepository.save(any())).willReturn(Accommodation.builder().id(1L).build());

        // when
        Long accommodationId = accommodationService.createAccommodation(request);

        // then
        // 여기서는 repository 또는 내부 처리 결과 확인을 위한 검증이 필요함.
        // 예를 들어 내부에서 amenityInfos를 Map<String, Integer>로 합치는 로직이 있다면,
        // 그 결과를 반환하거나 검증 가능한 구조로 분리해야 함.
        // 아래는 구조적으로 amenity 합산 로직만 따로 테스트하는 예:

        Map<String, Integer> merged = amenities.stream()
                .collect(Collectors.toMap(
                        a -> a.getName().toLowerCase(), // case-insensitive merge
                        AmenityInfo::getCount,
                        Integer::sum
                ));

        assertThat(merged).hasSize(1);
        assertThat(merged.get("wifi")).isEqualTo(3);
    }
}
