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

        ArgumentCaptor<List<AccommodationAmenity>> captor = ArgumentCaptor.forClass(List.class);
        verify(accommodationAmenityRepository, times(1)).saveAll(captor.capture());

        List<AccommodationAmenity> savedAmenities = captor.getValue();
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
    @DisplayName("DB에 사전에 등록되지 않은 어메니티는 등록할 수 없다.")
    void ignoresInvalidAmenityNames() {
        // given
        List<AmenityInfo> amenities = List.of(
                new AmenityInfo("WIFI", 1),
                new AmenityInfo("UNKNOWN", 1),
                new AmenityInfo("POOL", 1)
        );

        CreateAccommodationDto request = CreateAccommodationDto.builder()
                .hostId(1L)
                .amenityInfos(amenities)
                .type("HOTEL_ROOM")
                .build();

        Member member = Member.builder()
                .id(1L)
                .nickname("testMember")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        List<Amenity> amenityList = List.of(
                Amenity.builder().id(1L).name(AmenityType.WIFI).build(),
                Amenity.builder().id(2L).name(AmenityType.POOL).build());
        given(amenityRepository.findByNameIn(any())).willReturn(amenityList);

        Accommodation accommodation = Accommodation.builder().id(1L).build();
        given(accommodationRepository.save(any())). willReturn(accommodation);

        ArgumentCaptor<List<AccommodationAmenity>> captor = ArgumentCaptor.forClass(List.class);

        // when
        accommodationService.createAccommodation(request);

        // then
        verify(accommodationAmenityRepository).saveAll(captor.capture());

        List<AccommodationAmenity> saved = captor.getValue();
        List<AmenityType> types = saved.stream()
                .map(AccommodationAmenity::getAmenity)
                .map(Amenity::getName)
                .toList();

        assertThat(types).containsExactlyInAnyOrder(AmenityType.WIFI, AmenityType.POOL);
        assertThat(types).doesNotContain(AmenityType.UNKNOWN);
    }

    @Test
    @DisplayName("어메니티 갯수가 0개 이하면 저장되지 않는다 ")
    void ignoresAmenitiesWithNonPositiveCount() {
        // given
        List<AmenityInfo> amenities = List.of(
                new AmenityInfo("WIFI", 1),
                new AmenityInfo("POOL", 0),       // 무시됨
                new AmenityInfo("PARKING", -1)    // 무시됨
        );
        CreateAccommodationDto request = CreateAccommodationDto.builder()
                .hostId(1L)
                .amenityInfos(amenities)
                .type("HOTEL_ROOM")
                .build();

        Member member = Member.builder()
                .id(1L)
                .nickname("testMember")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        given(amenityRepository.findByNameIn(Set.of(AmenityType.WIFI)))
                .willReturn(List.of(Amenity.builder().id(1L).name(AmenityType.WIFI).build()));

        Accommodation accommodation = Accommodation.builder().id(1L).build();
        given(accommodationRepository.save(any())). willReturn(accommodation);

        // when
        accommodationService.createAccommodation(request);

        // then
        ArgumentCaptor<List<AccommodationAmenity>> captor = ArgumentCaptor.forClass(List.class);
        verify(accommodationAmenityRepository).saveAll(captor.capture());

        List<AccommodationAmenity> savedAmenities = captor.getValue();
        assertThat(savedAmenities)
                .hasSize(1)
                .extracting(AccommodationAmenity::getAmenity)
                .extracting(Amenity::getName)
                .containsOnly(AmenityType.WIFI);
    }
}
