package kr.kro.airbob.domain.payment;

import jakarta.servlet.http.HttpSession;
import kr.kro.airbob.domain.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    @PostMapping("/saveAmount")
    public ResponseEntity<?> saveAmountInSession(HttpSession session, @RequestBody PaymentRequestDto.SaveAmountRequest saveAmountRequest) {
        session.setAttribute(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());
        return ResponseEntity.ok("Payment temp save successful");
    }

    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(HttpSession session, @RequestBody PaymentRequestDto.SaveAmountRequest saveAmountRequest) {

        long amount = (long) session.getAttribute(saveAmountRequest.getOrderId());
        // 결제 전의 금액과 결제 후의 금액이 같은지 검증
        if(amount != saveAmountRequest.getAmount())
            return ResponseEntity.badRequest().body("결제 금액 정보가 유효하지 않습니다.");

        // 검증에 사용했던 세션은 삭제
        session.removeAttribute(saveAmountRequest.getOrderId());

        return ResponseEntity.ok("Payment is valid");
    }

}
