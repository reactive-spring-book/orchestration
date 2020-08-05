package rsb.rsocket.metadata.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import rsb.rsocket.BootifulProperties;

@Configuration
class ClientConfiguration {

	@Bean
	RSocketRequester rsocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder//
				.dataMimeType(MimeTypeUtils.APPLICATION_JSON)// <1>
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())//
				.block();
	}

}
