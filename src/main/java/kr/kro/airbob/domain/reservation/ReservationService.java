package kr.kro.airbob.domain.reservation;

import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import kr.kro.airbob.domain.reservation.entity.Reservation;
import kr.kro.airbob.domain.reservation.entity.ReservedDate;
import kr.kro.airbob.domain.reservation.repository.ReservationRepository;
import kr.kro.airbob.domain.reservation.repository.ReservedDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservedDateRepository reservedDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Optional<Long> createReservation(Long accommodationId, ReservationRequestDto.CreateReservationDto createReservationDto) {
        // todo 로그인 적용
        // todo 커스텀 예외 만들기
        Long userId = 1L;
        Member guest = memberRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 숙소입니다."));

        List<ReservedDate> alreadyReservedDates = reservedDateRepository.findReservedDates(accommodationId, createReservationDto.getCheckInDate(), createReservationDto.getCheckOutDate());

        if (!alreadyReservedDates.isEmpty()) {
            return Optional.empty();
        }

        long totalReservationDays = ChronoUnit.DAYS.between(
                createReservationDto.getCheckInDate(),
                createReservationDto.getCheckOutDate()
        );

        long totalPrice = totalReservationDays * accommodation.getBasePrice();

        reservationRepository.save(Reservation.createReservation(createReservationDto, accommodation, guest, (int) totalPrice));

        List<ReservedDate> reservedDates = new ArrayList<>();
        for (int n = 0; n < totalReservationDays; n++) {
            ReservedDate reservedDate = ReservedDate.builder()
                    .reservedAt(createReservationDto.getCheckInDate().plusDays(n))
                    .accommodation(accommodation)
                    .build();
            reservedDates.add(reservedDate);
        }
        reservedDateRepository.saveAll(reservedDates);
        return Optional.of(accommodation.getId());


    }

}
