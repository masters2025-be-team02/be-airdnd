package kr.kro.airbob.domain.accommodation;

import jakarta.validation.Valid;
import java.util.Map;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import kr.kro.airbob.domain.accommodation.entity.Accommodation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> registerAccommodation(@RequestBody @Valid AccommodationRequest.CreateAccommodationDto request){
        Long savedAccommodationId = accommodationService.createAccommodation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", savedAccommodationId));
    }
}

