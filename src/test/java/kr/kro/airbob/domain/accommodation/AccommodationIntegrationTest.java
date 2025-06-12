package kr.kro.airbob.domain.accommodation;

import static kr.kro.airbob.domain.accommodation.common.AmenityType.IRON;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.MICROWAVE;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.PARKING;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.SHAMPOO;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.TV;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.WIFI;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import kr.kro.airbob.domain.accommodation.common.AccommodationType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.CreateAccommodationDto;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.UpdateAccommodationDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Transactional
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccommodationIntegrationTest {

    @Autowired
    private AccommodationService accommodationService;

    @Autowired
    private AccommodationRepository accommodationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AmenityRepository amenityRepository;
    @Autowired
    private AccommodationAmenityRepository accommodationAmenityRepository;
    @Autowired
    private OccupancyPolicyRepository occupancyPolicyRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("pass");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
    }

    @BeforeEach
    void setupDummyData() {
        Member member = Member.builder()
                .nickname("테스트 사용자")
                .email("test@example.com")
                .build();
        memberRepository.save(member);

        amenityRepository.save(Amenity.builder().name(WIFI).build());
        amenityRepository.save(Amenity.builder().name(PARKING).build());
        amenityRepository.save(Amenity.builder().name(IRON).build());
    }

    @Test
    @DisplayName("중복된 이름의 Amenity가 들어오면 count가 합산된다")
    void countDuplicatedAmenity() {
        // given
        List<AmenityInfo> amenities = List.of(
                new AmenityInfo("WIFI", 1),
                new AmenityInfo("wifi", 2)
        );

        CreateAccommodationDto request = CreateAccommodationDto.builder()
                .name("테스트 숙소")
                .description("설명")
                .basePrice(100000)
                .hostId(1L)
                .type("HOSTEL")
                .amenityInfos(amenities)
                .build();

        // when
        Long accommodationId = accommodationService.createAccommodation(request);

        // then
        List<AccommodationAmenity> savedAmenities = accommodationAmenityRepository.findAllById(
                Collections.singleton(accommodationId));

        assertThat(savedAmenities).hasSize(1);

        AccommodationAmenity amenity = savedAmenities.get(0);
        assertThat(amenity.getAmenity().getName()).isEqualTo(WIFI);
        assertThat(amenity.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("모든 정보가 주어졌을 때 숙소, 주소, 정책, 어메니티가 모두 정상적으로 업데이트된다")
    void updateAccommodationSuccess() {
        // given - 기존 엔티티 저장
        Address originalAddress = addressRepository.save(createAddress("Seoul"));
        OccupancyPolicy originalPolicy = occupancyPolicyRepository.save(createPolicy());
        Accommodation accommodation = accommodationRepository.save(createAccommodation(originalAddress, originalPolicy));

        Amenity wifi = Amenity.builder().name(WIFI).build();
        Amenity tv = Amenity.builder().name(TV).build();
        Amenity shampoo = Amenity.builder().name(SHAMPOO).build();
        Amenity microwave = Amenity.builder().name(MICROWAVE).build();
        amenityRepository.saveAll(List.of(wifi, tv, shampoo, microwave));

        List<AccommodationAmenity> amenities = List.of(
                AccommodationAmenity.builder().accommodation(accommodation).amenity(wifi).build(),
                AccommodationAmenity.builder().accommodation(accommodation).amenity(tv).build(),
                AccommodationAmenity.builder().accommodation(accommodation).amenity(shampoo).build(),
                AccommodationAmenity.builder().accommodation(accommodation).amenity(microwave).build()
        );
        accommodationAmenityRepository.saveAll(amenities);

        Long accommodationId = accommodation.getId();

        // 업데이트 요청
        AccommodationRequest.UpdateAccommodationDto request = AccommodationRequest.UpdateAccommodationDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .basePrice(180000)
                .type("GUESTHOUSE")
                .addressInfo(AccommodationRequest.AddressInfo.builder()
                        .postalCode(54321)
                        .city("Busan")
                        .country("KR")
                        .district("Haeundae")
                        .street("Beach-ro")
                        .detail("202")
                        .build())
                .occupancyPolicyInfo(AccommodationRequest.OccupancyPolicyInfo.builder()
                        .maxOccupancy(6)
                        .adultOccupancy(4)
                        .childOccupancy(1)
                        .infantOccupancy(1)
                        .petOccupancy(0)
                        .build())
                .amenityInfos(List.of(
                        AmenityInfo.builder().name("SHAMPOO").count(2).build(),
                        AmenityInfo.builder().name("MICROWAVE").count(1).build()))
                .build();

        // when
        accommodationService.updateAccommodation(accommodationId, request);

        // then
        Accommodation updated = accommodationRepository.findById(accommodationId).get();

        // 숙소 정보 확인
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getBasePrice()).isEqualTo(180000);
        assertThat(updated.getType().name()).isEqualTo("GUESTHOUSE");

        // 주소 정보 확인
        Address newAddress = updated.getAddress();
        assertThat(newAddress.getCity()).isEqualTo("Busan");
        assertThat(newAddress.getPostalCode()).isEqualTo(54321);
        assertThat(newAddress.getDetail()).isEqualTo("202");

        // 점유 정책 정보 확인
        OccupancyPolicy policy = updated.getOccupancyPolicy();
        assertThat(policy.getMaxOccupancy()).isEqualTo(6);
        assertThat(policy.getAdultOccupancy()).isEqualTo(4);
        assertThat(policy.getInfantOccupancy()).isEqualTo(1);

        // 어메니티 정보 확인
        List<AccommodationAmenity> accommodationAmenityList = accommodationAmenityRepository.findAllByAccommodationId(accommodationId);
        assertThat(accommodationAmenityList).hasSize(2);
        assertThat(accommodationAmenityList).extracting("amenity")
                .extracting("name").containsExactlyInAnyOrder(SHAMPOO, MICROWAVE);
    }

    @Test
    @DisplayName("주소 정보가 null이면 기존 주소가 유지된다")
    void ifAddressNullShouldNotChangeAddress() {
        // given
        Address originalAddress = addressRepository.save(createAddress("Seoul"));
        OccupancyPolicy policy = occupancyPolicyRepository.save(createPolicy());
        Accommodation accommodation = accommodationRepository.save(createAccommodation(originalAddress, policy));
        Long id = accommodation.getId();

        UpdateAccommodationDto updateRequest = UpdateAccommodationDto.builder()
                .name("New Name")
                .addressInfo(null)
                .build();

        // when
        accommodationService.updateAccommodation(id, updateRequest);

        // then
        Accommodation updated = accommodationRepository.findById(id).get();
        assertThat(updated.getAddress().getCity()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("인원 정책 정보가 null이면 기존 정책이 유지된다")
    void ifPolicyNullShouldNotChangePolicy() {
        // given
        Address address = addressRepository.save(createAddress("Busan"));
        OccupancyPolicy originalPolicy = occupancyPolicyRepository.save(createPolicy());
        Accommodation accommodation = accommodationRepository.save(createAccommodation(address, originalPolicy));
        Long id = accommodation.getId();

        UpdateAccommodationDto updateRequest = UpdateAccommodationDto.builder()
                .name("Updated Name")
                .occupancyPolicyInfo(null)
                .build();

        // when
        accommodationService.updateAccommodation(id, updateRequest);

        // then
        Accommodation updated = accommodationRepository.findById(id).get();
        assertThat(updated.getOccupancyPolicy().getMaxOccupancy()).isEqualTo(5);
        assertThat(updated.getOccupancyPolicy().getAdultOccupancy()).isEqualTo(3);
    }

    @Test
    @DisplayName("어메니티 리스트가 비어있으면 기존 어메니티가 유지된다")
    void emptyAmenityListShouldNotDeleteAmenities() {
        // given
        Address address = addressRepository.save(createAddress("Incheon"));
        OccupancyPolicy policy = occupancyPolicyRepository.save(createPolicy());
        Accommodation accommodation = accommodationRepository.save(createAccommodation(address, policy));

        Amenity wifi = Amenity.builder().name(WIFI).build();
        Amenity tv = Amenity.builder().name(TV).build();
        amenityRepository.saveAll(List.of(wifi, tv));

        List<AccommodationAmenity> amenities = List.of(
                AccommodationAmenity.builder().accommodation(accommodation).amenity(wifi).build(),
                AccommodationAmenity.builder().accommodation(accommodation).amenity(tv).build()
        );
        accommodationAmenityRepository.saveAll(amenities);

        Long accommodationId = accommodation.getId();

        var updateRequest = AccommodationRequest.UpdateAccommodationDto.builder()
                .name("Updated")
                .amenityInfos(List.of()) // 핵심
                .build();

        // when
        accommodationService.updateAccommodation(accommodationId, updateRequest);

        // then
        List<AccommodationAmenity> accommodationAmenityList = accommodationAmenityRepository.findAllByAccommodationId(accommodationId);
        assertThat(accommodationAmenityList).hasSize(2);
        assertThat(accommodationAmenityList).extracting("amenity")
                .extracting("name").containsExactlyInAnyOrder(WIFI, TV);
    }

    private Address createAddress(String city) {
        return Address.builder()
                .postalCode(12345)
                .city(city)
                .country("KR")
                .district("District")
                .street("Street")
                .detail("Detail")
                .build();
    }

    private OccupancyPolicy createPolicy() {
        return OccupancyPolicy.builder()
                .maxOccupancy(5)
                .adultOccupancy(3)
                .childOccupancy(1)
                .infantOccupancy(0)
                .petOccupancy(0)
                .build();
    }
    private Accommodation createAccommodation(Address address, OccupancyPolicy policy) {
        return Accommodation.builder()
                .name("Test House")
                .description("desc")
                .basePrice(100000)
                .thumbnailUrl("url")
                .type(AccommodationType.HOUSE)
                .address(address)
                .occupancyPolicy(policy)
                .member(null)
                .build();
    }
}

