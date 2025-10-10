package kr.kro.airbob.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class PaymentRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SaveAmountRequest {
        @NotNull
        private String orderId;
        @NotNull
        private long amount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ConfirmPaymentRequest {
        private String orderId;
        private long amount;
        private String paymentKey;
    }



}
