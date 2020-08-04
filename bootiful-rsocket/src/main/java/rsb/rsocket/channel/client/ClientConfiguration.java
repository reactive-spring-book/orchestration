package rsb.rsocket.channel.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder.connectTcp(properties.getRsocket().getHostname(),
				properties.getRsocket().getPort()).block();
	}

}