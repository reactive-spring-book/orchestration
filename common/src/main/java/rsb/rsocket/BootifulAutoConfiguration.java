package rsb.rsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BootifulProperties.class)
class BootifulAutoConfiguration {

	@Bean // <1>
	EncodingUtils encodingUtils(ObjectMapper objectMapper) {
		return new EncodingUtils(objectMapper);
	}

}
