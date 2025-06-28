package kr.kro.airbob.geo.dto;

public record IpInfoResponse(
	String ip,
	String asn, // AS 번호
	String asName, // AS 이름
	String asDomain, // AS 도메인
	String countryCode, // ISO 국가 코드 ex) US
	String country, // 국가 이름
	String continentCode, // ex) NA
	String continent // ex) North America
) {
}
