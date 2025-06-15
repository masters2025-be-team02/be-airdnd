package kr.kro.airbob.domain.accommodation.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.kro.airbob.domain.accommodation.common.AccommodationType;
import kr.kro.airbob.domain.accommodation.common.AmenityType;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest.AccommodationSearchConditionDto;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import kr.kro.airbob.domain.accommodation.entity.QAccommodation;
import kr.kro.airbob.domain.accommodation.entity.QAccommodationAmenity;
import kr.kro.airbob.domain.accommodation.entity.QAmenity;
import kr.kro.airbob.domain.accommodation.entity.QOccupancyPolicy;
import kr.kro.airbob.domain.reservation.QReservedDate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    public List<Accommodation> search(AccommodationSearchConditionDto condition) {
        QAccommodation accommodation = QAccommodation.accommodation;
        QOccupancyPolicy occupancyPolicy = QOccupancyPolicy.occupancyPolicy;
        QAccommodationAmenity accommodationAmenity = QAccommodationAmenity.accommodationAmenity;
        QAmenity amenity = QAmenity.amenity;
        QReservedDate reservedDate = QReservedDate.reservedDate;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getCity() != null) {
            builder.and(accommodation.address.city.eq(condition.getCity()));
        }

        if (condition.getMinPrice() != null) {
            builder.and(accommodation.basePrice.goe(condition.getMinPrice()));
        }

        if (condition.getMaxPrice() != null) {
            builder.and(accommodation.basePrice.loe(condition.getMaxPrice()));
        }

        if (condition.getAccommodationTypes() != null && !condition.getAccommodationTypes().isEmpty()) {
            builder.and(accommodation.type.in(AccommodationType.valuesOf(condition.getAccommodationTypes())));
        }

        if (condition.getGuestCount() != null) {
            builder.and(occupancyPolicy.maxOccupancy.goe(condition.getGuestCount()));
        }

        if (condition.getAmenityTypes() != null && !condition.getAmenityTypes().isEmpty()) {
            builder.and(amenity.name.in(AmenityType.valuesOf(condition.getAmenityTypes())));
        }

        if (condition.getCheckIn() != null && condition.getCheckOut() != null) {
            builder.and(
                    jpaQueryFactory.selectFrom(reservedDate)
                            .where(reservedDate.accommodation.eq(accommodation)
                                    .and(reservedDate.reservedAt.between(condition.getCheckIn(), condition.getCheckOut())))
                            .notExists()
            );
        }

        return jpaQueryFactory
                .select(accommodation)
                .distinct()
                .from(accommodation)
                .join(accommodation.occupancyPolicy, occupancyPolicy)
                .leftJoin(accommodationAmenity).on(accommodationAmenity.accommodation.eq(accommodation))
                .leftJoin(accommodationAmenity.amenity, amenity)
                .where(builder)
                .fetch();
    }
}
