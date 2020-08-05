package rsb.rsocket.requestresponse.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties, // <1>
			RSocketRequester.Builder builder) {// <2>
		return builder.connectTcp(properties.getRsocket().getHostname(),
				properties.getRsocket().getPort()).block();
	}

}