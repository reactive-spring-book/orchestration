package rsb.rsocket.bidirectional.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import rsb.rsocket.BootifulProperties;

@SpringBootApplication
public class BidirectionalApplication {

	public static void main(String[] args) {
		System.setProperty("spring.rsocket.server.port", "9090");
		SpringApplication.run(BidirectionalApplication.class, args);
	}

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