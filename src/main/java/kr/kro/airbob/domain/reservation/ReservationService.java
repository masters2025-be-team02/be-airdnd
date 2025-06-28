package kr.kro.airbob.domain.reservation;

import static kr.kro.airbob.search.event.AccommodationIndexingEvents.*;

import jakarta.annotation.PostConstruct;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.reservation.common.ReservationStatus;
import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import kr.kro.airbob.domain.reservation.entity.Reservation;
import kr.kro.airbob.domain.reservation.entity.ReservedDate;
import kr.kro.airbob.domain.reservation.exception.ReservationNotFoundException;
import kr.kro.airbob.domain.reservation.repository.ReservationRepository;
import kr.kro.airbob.domain.reservation.repository.ReservedDateRepository;
import kr.kro.airbob.search.event.AccommodationIndexingEvents;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservedDateRepository reservedDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final MemberRepository memberRepository;
    private final RedissonClient redissonClient;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean preReserveDates(Long userId, Long accommodationId, ReservationRequestDto.CreateReservationDto createReservationDto) {
        long daysToReserve = ChronoUnit.DAYS.between(createReservationDto.getCheckInDate(), createReservationDto.getCheckOutDate());
        Duration lockTtl = Duration.ofSeconds(60);

        List<String> acquiredLockKeys = new ArrayList<>();

        for (int n = 0; n < daysToReserve - 1; n++) {
            LocalDate dayToReserve = createReservationDto.getCheckInDate().plusDays(n);
            String lockKey = "lock:accommodation:" + accommodationId + ":dayToReserve:" + dayToReserve;

            RBucket<String> lockBucket = redissonClient.getBucket(lockKey);
            boolean acquired = lockBucket.setIfAbsent(String.valueOf(userId), lockTtl);

            if (!acquired) {
                // 락 잡기 실패 → 지금까지 잡은 락 해제
                for (String key : acquiredLockKeys) {
                    RBucket<String> bucket = redissonClient.getBucket(key);
                    String lockOwner = bucket.get();
                    if (String.valueOf(userId).equals(lockOwner)) {
                        bucket.delete();
                    }
                }
                return false;
            }

            acquiredLockKeys.add(lockKey);
        }

        // 2. DB에 실제 예약된 날짜가 있는지 확인
        List<ReservedDate> alreadyReservedDates = reservedDateRepository.findReservedDates(
                accommodationId, createReservationDto.getCheckInDate(), createReservationDto.getCheckOutDate());

        if (!alreadyReservedDates.isEmpty()) {
            return false; // 예약 불가
        }

        // 3. checkin checkout 날짜에 대해 예약 처리 (임시 예약 상태로 처리)
        List<ReservedDate> preReservedDates = new ArrayList<>();

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(AccommodationNotFoundException::new);

        for (int n = 0; n < daysToReserve - 1; n++) {
            ReservedDate reservedDate = ReservedDate.builder()
                    .reservedAt(createReservationDto.getCheckInDate().plusDays(n))
                    .status(ReservationStatus.PENDING)
                    .accommodation(accommodation)
                    .build();
            preReservedDates.add(reservedDate);
        }
        reservedDateRepository.saveAll(preReservedDates);

        return true; // 임시 예약 완료
    }

    @Transactional
    public Long createReservation(Long memberId, Long accommodationId, ReservationRequestDto.CreateReservationDto createReservationDto) {
        Member guest = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(AccommodationNotFoundException::new);

        long totalReservationDays = ChronoUnit.DAYS.between(
                createReservationDto.getCheckInDate(),
                createReservationDto.getCheckOutDate()
        );

        long totalPrice = totalReservationDays * accommodation.getBasePrice();

        //1. 예약 확정 저장
        Reservation savedReservation = reservationRepository.save(Reservation.createReservation(createReservationDto, accommodation, guest, (int) totalPrice));

        // 2. ReservedDate 상태 업데이트
        List<ReservedDate> reservedDates = reservedDateRepository.findReservedDates(
                accommodationId, createReservationDto.getCheckInDate(), createReservationDto.getCheckOutDate());

        for (ReservedDate reservedDate : reservedDates) {
            reservedDate.completeReservation();
        }

        eventPublisher.publishEvent(new ReservationChangedEvent(accommodationId));

        return savedReservation.getId();
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        reservedDateRepository.deleteReservedDates(reservation.getAccommodation().getId(),
                LocalDate.of(reservation.getCheckIn().getYear(), reservation.getCheckIn().getMonth(), reservation.getCheckIn().getDayOfMonth()),
                LocalDate.of(reservation.getCheckOut().getYear(), reservation.getCheckOut().getMonth(), reservation.getCheckOut().getDayOfMonth()));

        reservationRepository.delete(reservation);

        eventPublisher.publishEvent(new ReservationChangedEvent(reservation.getAccommodation().getId()));
    }
}
