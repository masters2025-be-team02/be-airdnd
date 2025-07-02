package kr.kro.airbob.domain.reservation;

import java.time.LocalDate;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.exception.AccommodationNotFoundException;
import kr.kro.airbob.domain.accommodation.repository.AccommodationRepository;
import kr.kro.airbob.domain.member.Member;
import kr.kro.airbob.domain.member.MemberRepository;
import kr.kro.airbob.domain.member.exception.MemberNotFoundException;
import kr.kro.airbob.domain.reservation.dto.ReservationRequestDto;
import kr.kro.airbob.domain.reservation.entity.Reservation;
import kr.kro.airbob.domain.reservation.entity.ReservedDate;
import kr.kro.airbob.domain.reservation.exception.AlreadyReservedException;
import kr.kro.airbob.domain.reservation.repository.ReservationRepository;
import kr.kro.airbob.domain.reservation.repository.ReservedDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservedDateRepository reservedDateRepository;
    private final AccommodationRepository accommodationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createReservation(Long accommodationId, ReservationRequestDto.CreateReservationDto createReservationDto, long memberId) {
        Member guest = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(AccommodationNotFoundException::new);

        List<LocalDate> alreadyReservedDates = reservedDateRepository.findReservedDates(accommodationId, createReservationDto.getCheckInDate(), createReservationDto.getCheckOutDate());

        if (!alreadyReservedDates.isEmpty()) {
            throw new AlreadyReservedException();
        }

        long totalReservationDays = ChronoUnit.DAYS.between(
                createReservationDto.getCheckInDate(),
                createReservationDto.getCheckOutDate()
        );

        long totalPrice = totalReservationDays * accommodation.getBasePrice();

        Reservation savedReservation = reservationRepository.save(Reservation.createReservation(createReservationDto, accommodation, guest, (int) totalPrice));

        List<ReservedDate> reservedDates = new ArrayList<>();
        for (int n = 0; n < totalReservationDays; n++) {
            ReservedDate reservedDate = ReservedDate.builder()
                    .reservedAt(createReservationDto.getCheckInDate().plusDays(n))
                    .accommodation(accommodation)
                    .build();
            reservedDates.add(reservedDate);
        }
        reservedDateRepository.saveAll(reservedDates);
        return savedReservation.getId();
    }

}
