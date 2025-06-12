package kr.kro.airbob.domain.accommodation.common;

public enum AmenityType {
    WIFI,               // 무선 인터넷
    AIR_CONDITIONER,    // 에어컨
    HEATING,            // 난방
    KITCHEN,            // 주방
    WASHER,             // 세탁기
    DRYER,              // 건조기
    PARKING,            // 주차 공간
    TV,                 // TV
    HAIR_DRYER,         // 헤어드라이어
    IRON,               // 다리미
    SHAMPOO,            // 샴푸
    BED_LINENS,         // 침구류
    EXTRA_PILLOWS,      // 추가 베개 및 담요
    CRIB,               // 아기 침대
    HIGH_CHAIR,         // 아기 식탁의자
    DISHWASHER,         // 식기세척기
    COFFEE_MACHINE,     // 커피 머신
    MICROWAVE,          // 전자레인지
    REFRIGERATOR,       // 냉장고
    ELEVATOR,           // 엘리베이터
    POOL,               // 수영장
    HOT_TUB,            // 온수 욕조
    GYM,                // 헬스장
    SMOKE_ALARM,        // 화재 경보기
    CARBON_MONOXIDE_ALARM, // 일산화탄소 경보기
    FIRE_EXTINGUISHER,  // 소화기
    PETS_ALLOWED,       // 반려동물 허용
    OUTDOOR_SPACE,      // 야외 공간
    BBQ_GRILL,          // 바베큐 그릴
    BALCONY,             // 발코니
    UNKNOWN;

    public static boolean isValid(String name) {
        if (name == null) return false;
        try {
            AmenityType.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
