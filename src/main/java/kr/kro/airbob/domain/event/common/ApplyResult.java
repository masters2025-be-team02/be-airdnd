package kr.kro.airbob.domain.event.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplyResult {
    SUCCESS("응모에 성공하였습니다."),
    DUPLICATE("이미 응모한 사용자입니다."),
    FULL("이벤트 응모가 마감되었습니다.");

    private String message;
}
