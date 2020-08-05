package rsb.rsocket.metadata.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.codec.StringDecoder;
import rsb.rsocket.metadata.Constants;

@SpringBootApplication
public class MetadataApplication {

	@SneakyThrows
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(MetadataApplication.class, args);
	}

}
