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

	@Bean
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
		return strategies -> strategies//
				.metadataExtractorRegistry(registry -> {
					registry.metadataToExtract(Constants.CLIENT_ID, String.class,
							Constants.CLIENT_ID_HEADER);
					registry.metadataToExtract(Constants.LANG, String.class,
							Constants.LANG_HEADER);
				})//
				.decoders(decoders -> decoders.add(StringDecoder.allMimeTypes()));
	}

}
