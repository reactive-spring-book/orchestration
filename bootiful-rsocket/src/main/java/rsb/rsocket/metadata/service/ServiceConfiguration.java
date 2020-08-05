package rsb.rsocket.metadata.service;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.StringDecoder;
import rsb.rsocket.metadata.Constants;

@Configuration
class ServiceConfiguration {

	@Bean
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
		return strategies -> strategies//
				.metadataExtractorRegistry(registry -> {
					// <1>
					registry.metadataToExtract(Constants.CLIENT_ID, String.class,
							Constants.CLIENT_ID_HEADER);
					registry.metadataToExtract(Constants.LANG, String.class,
							Constants.LANG_HEADER);
				})//
				.decoders(decoders -> decoders.add(StringDecoder.allMimeTypes()));
	}

}
