---
name: "✨Feature Request"
about: 새로운 기능이나 개선 사항을 제안합니다.
title: "[Feat]"
labels: feat
assignees: ''

---

## :dart: 목적

[//]: # (HTTP 요청 메시지의 Request-Line을 파싱하여 `method`, `URI`, `HTTP-version`을 분리하고,)

[//]: # (RFC 7230 문법에 따라 유효성 검사를 수행한다.)

## :jigsaw: 작업 내용

[//]: # (- [ ]  `HttpParser` 클래스 생성)

Issue Feat 템플릿

[//]: # (- [ ]  `parseRequestLine&#40;const std::string&&#41;` 메서드 구현)

[//]: # (- [ ]  잘못된 요청 시 예외 처리 &#40;`InvalidRequestException`&#41;)

[//]: # (- [ ]  `HttpRequest` 모델 클래스에 method/path/version 필드 추가)

[//]: # (- [ ]  GTest 기반 단위 테스트 작성 &#40;정상/비정상 입력&#41;)

## :white_check_mark: 완료 조건

[//]: # (- 정상적인 요청 라인이 올바르게 파싱되어 객체로 생성됨)

[//]: # (- 비정상적인 요청&#40;공백 부족, HTTP 버전 오류 등&#41;에 대해 예외 처리 동작함)

[//]: # (- GTest 테스트 전부 통과)

[//]: # (- 이후 모듈&#40;Validator, Router 등&#41;과 연결될 수 있는 구조 확보)
