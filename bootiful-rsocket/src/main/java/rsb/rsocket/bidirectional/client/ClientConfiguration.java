package rsb.rsocket.bidirectional.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.ClientRSocketFactoryConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	// <1>
	@Bean
	ClientRSocketFactoryConfigurer clientRSocketFactoryConfigurer(
			HealthController healthController, RSocketStrategies strategies) {
		return RSocketMessageHandler.clientResponder(strategies, healthController);
	}

	@Bean
	RSocketRequester rSocketRequester(ClientRSocketFactoryConfigurer configurer,
			RSocketRequester.Builder builder, BootifulProperties properties) {

		return builder//
				.rsocketFactory(configurer)// <2>
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())//
				.block();
	}

}
