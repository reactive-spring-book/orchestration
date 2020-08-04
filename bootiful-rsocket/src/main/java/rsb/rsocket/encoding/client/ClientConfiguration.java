package rsb.rsocket.encoding.client;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	// make sure we are guaranteed to be the last in
	// the line
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
		return strategies -> strategies.decoder(new Jackson2JsonDecoder())
				.encoder(new Jackson2JsonEncoder());
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder//
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort()) //
				.block();
	}

}