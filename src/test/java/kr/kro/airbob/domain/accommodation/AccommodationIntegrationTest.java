package kr.kro.airbob.domain.accommodation;

import static kr.kro.airbob.domain.accommodation.common.AmenityType.IRON;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.PARKING;
import static kr.kro.airbob.domain.accommodation.common.AmenityType.WIFI;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AmenityInfo;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.CreateAccommodationDto;
import kr.kro.airbob.domain.accommodation.entity.AccommodationAmenity;
import kr.kro.airbob.domain.accommodation.entity.Amenity;
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
}

