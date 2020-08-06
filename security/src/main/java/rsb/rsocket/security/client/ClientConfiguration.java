package rsb.rsocket.security.client;

import io.rsocket.metadata.WellKnownMimeType;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.security.GreetingResponse;

@Log4j2
@Configuration
class ClientConfiguration {

	private final MimeType mimeType = MimeTypeUtils
			.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

	private final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata(
			"jlong", "pw");

	@Bean
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
		return strategies -> strategies.encoder(new SimpleAuthenticationEncoder());
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder//
				.setupMetadata(this.credentials, this.mimeType) // OPTIONAL!
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())
				.block();
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(RSocketRequester greetings) {
		return args -> greetings//
				.route("greetings")//
				.metadata(this.credentials, this.mimeType)//
				.data(Mono.empty())//
				.retrieveFlux(GreetingResponse.class)//
				.subscribe(gr -> log.info("secured response: " + gr.toString()));
	}

}
