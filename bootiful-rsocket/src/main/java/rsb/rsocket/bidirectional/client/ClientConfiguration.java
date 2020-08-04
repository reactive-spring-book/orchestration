package rsb.rsocket.bidirectional.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	@Bean
	RSocketRequester rSocketRequester(HealthController healthController,
			RSocketStrategies strategies, RSocketRequester.Builder builder,
			BootifulProperties properties) {

		var configurer = RSocketMessageHandler.clientResponder(strategies,
				healthController);

		return builder//
				.rsocketFactory(configurer)//
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())//
				.block();
	}

}
