package rsb.rsocket.metadata.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;

import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class MetadataApplication {

	private final BootifulProperties properties;

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(MetadataApplication.class, args);
		System.in.read();
	}

	@Bean
	Client one(EncodingUtils encodingUtils) {
		return new Client(this.properties, encodingUtils, UUID.randomUUID().toString());
	}

	@Bean
	Client two(EncodingUtils encodingUtils) {
		return new Client(this.properties, encodingUtils, UUID.randomUUID().toString());
	}

}
