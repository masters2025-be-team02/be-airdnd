package kr.kro.airbob.domain.reservation.common;

import lombok.Getter;

@Getter
public enum ReservationTime {

    CHECK_IN(15,0),
    CHECK_OUT(11,0);

    private final int hour;
    private final int minute;

    ReservationTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

}
