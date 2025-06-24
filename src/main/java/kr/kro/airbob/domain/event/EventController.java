package kr.kro.airbob.domain.event;

import static kr.kro.airbob.domain.event.common.ApplyResult.DUPLICATE;
import static kr.kro.airbob.domain.event.common.ApplyResult.FULL;
import static kr.kro.airbob.domain.event.common.ApplyResult.SUCCESS;

import com.sun.net.httpserver.Authenticator.Success;
import jakarta.servlet.http.HttpServletRequest;
import kr.kro.airbob.domain.event.common.ApplyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @PostMapping("/{eventId}")
    public ResponseEntity<String> applyEvent(@PathVariable Long eventId, HttpServletRequest request) {
        Long memberId = (Long) request.getAttribute("memberId");
        ApplyResult applyResult = eventService.applyToEvent(eventId, memberId);

        switch (applyResult) {
            case SUCCESS -> {
                eventService.consumeQueue(eventId);
                return ResponseEntity.ok(SUCCESS.getMessage());
            }
            case DUPLICATE -> {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(DUPLICATE.getMessage());
            }
            case FULL -> {
                return ResponseEntity.status(HttpStatus.GONE).body(FULL.getMessage());
            }
            default -> {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류입니다.");
            }
        }
    }
}
