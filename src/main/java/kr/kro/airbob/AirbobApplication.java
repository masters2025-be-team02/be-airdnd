package kr.kro.airbob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AirbobApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirbobApplication.class, args);
	}

}
