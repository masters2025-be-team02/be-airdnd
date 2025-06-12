package kr.kro.airbob.domain.accommodation;

import jakarta.validation.Valid;
import java.util.Map;
import kr.kro.airbob.domain.accommodation.dto.AccommodationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    //todo 이미지 저장 로직 추가
    @PostMapping
    public ResponseEntity<Map<String, Long>> registerAccommodation(@RequestBody @Valid AccommodationRequest.CreateAccommodationDto request){
        Long savedAccommodationId = accommodationService.createAccommodation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", savedAccommodationId));
    }

    @PatchMapping("/{accommodationId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateAccommodation(@PathVariable Long accommodationId, @RequestBody AccommodationRequest.UpdateAccommodationDto request){
        accommodationService.updateAccommodation(accommodationId, request);
    }

    @DeleteMapping("/{accommodationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccommodation(@PathVariable Long accommodationId) {
        accommodationService.deleteAccommodation(accommodationId);
    }
}

