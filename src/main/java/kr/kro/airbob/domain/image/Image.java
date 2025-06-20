package kr.kro.airbob.domain.image;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import kr.kro.airbob.common.domain.BaseEntity;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class Image extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String imageUrl;
}
