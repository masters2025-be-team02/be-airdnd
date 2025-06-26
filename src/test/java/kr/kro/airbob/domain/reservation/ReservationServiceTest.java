package kr.kro.airbob.domain.reservation;

import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import kr.kro.airbob.domain.reservation.repository.ReservationRepository;
import kr.kro.airbob.domain.reservation.repository.ReservedDateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservedDateRepository reservedDateRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

//    @Test
//    void preReserveDates_lockAcquisitionFail() {
//        // given
//        Long userId = 1L;
//        Long accommodationId = 1L;
//        LocalDate checkIn = LocalDate.of(2025, 6, 20);
//        LocalDate checkOut = LocalDate.of(2025, 6, 23);
//
//        ReservationRequestDto.CreateReservationDto dto =
//                ReservationRequestDto.CreateReservationDto.builder()
//                        .checkInDate(checkIn)
//                        .checkOutDate(checkOut)
//                        .build();
//
//
//        // 첫번째 락은 잡히고, 두번째 락 잡기 실패 시뮬레이션
//        // redissonClient.getBucket() 호출 시 항상 같은 mock 리턴
//        given(redissonClient.getBucket(anyString())).willReturn(lockBucket);
//
//        // 첫 번째 호출 true, 두 번째 호출 false (락 획득 실패)
//        given(lockBucket.setIfAbsent(anyString(), any(Duration.class)))
//                .willReturn(true)
//                .willReturn(false);
//
//        // when
//        boolean result = reservationService.preReserveDates(userId, accommodationId, dto);
//
//        // then
//        assertThat(result).isFalse();
//        // 락 해제(delete) 호출이 1번 이상 발생했는지 확인
//        verify(lockBucket, atLeastOnce()).delete();
//        // 예약 데이터 저장 호출 없어야 함
//        verify(reservedDateRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    void preReserveDates_alreadyReservedDatesExist() {
//        // given
//        Long userId = 1L;
//        Long accommodationId = 1L;
//        LocalDate checkIn = LocalDate.of(2025, 6, 20);
//        LocalDate checkOut = LocalDate.of(2025, 6, 23);
//
//        ReservationRequestDto.CreateReservationDto dto =
//                ReservationRequestDto.CreateReservationDto.builder()
//                        .checkInDate(checkIn)
//                        .checkOutDate(checkOut)
//                        .build();
//
//        given(redissonClient.getBucket(anyString())).willReturn(lockBucket);
//        given(lockBucket.setIfAbsent(anyString(), any(Duration.class))).willReturn(true);
//
//        // 이미 예약된 날짜가 존재하는 상황
//        given(reservedDateRepository.findReservedDates(accommodationId, checkIn, checkOut))
//                .willReturn(List.of(mock(ReservedDate.class)));
//
//        // when
//        boolean result = reservationService.preReserveDates(userId, accommodationId, dto);
//
//        // then
//        assertThat(result).isFalse();
//        // 예약 데이터 저장 호출 없어야 함
//        verify(reservedDateRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    void preReserveDates_success() {
//        // given
//        Long userId = 1L;
//        Long accommodationId = 1L;
//        LocalDate checkIn = LocalDate.of(2025, 6, 20);
//        LocalDate checkOut = LocalDate.of(2025, 6, 23);
//
//        ReservationRequestDto.CreateReservationDto dto =
//                ReservationRequestDto.CreateReservationDto.builder()
//                        .checkInDate(checkIn)
//                        .checkOutDate(checkOut)
//                        .build();
//
//        Accommodation accommodation = mock(Accommodation.class);
//        given(accommodationRepository.findById(accommodationId)).willReturn(Optional.of(accommodation));
//
//        given(redissonClient.getBucket(anyString())).willReturn(lockBucket);
//        given(lockBucket.setIfAbsent(anyString(), any(Duration.class))).willReturn(true);
//
//        // DB에 예약된 날짜 없음
//        given(reservedDateRepository.findReservedDates(accommodationId, checkIn, checkOut)).willReturn(Collections.emptyList());
//
//        // when
//        boolean result = reservationService.preReserveDates(userId, accommodationId, dto);
//
//        // then
//        assertThat(result).isTrue();
//        verify(reservedDateRepository).saveAll(anyList());
//    }

    @Test
    @DisplayName("존재하지 않는 유저가 예약하려고 하면 예외가 발생해야 한다.")
    void createReservation_shouldThrowException_whenMemberNotFound()  {
        // given
        Long memberId = 1L;
        Long accommodationId = 1L;
        ReservationRequestDto.CreateReservationDto request = mock(ReservationRequestDto.CreateReservationDto.class);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(memberId, accommodationId, request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 숙소를 예약하려고 하면 예외가 발생해야 한다.")
    void createReservation_shouldThrowException_whenAccommodationNotFound()  {
        // given
        Long memberId = 1L;
        Long accommodationId = 1L;
        ReservationRequestDto.CreateReservationDto request = mock(ReservationRequestDto.CreateReservationDto.class);

        Member member = mock(Member.class);

        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(member));

        given(accommodationRepository.findById(accommodationId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(memberId, accommodationId, request))
                .isInstanceOf(AccommodationNotFoundException.class)
                .hasMessage("존재하지 않는 숙소입니다.");
    }

}
