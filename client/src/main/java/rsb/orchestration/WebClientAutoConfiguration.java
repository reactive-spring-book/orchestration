package rsb.orchestration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@Configuration
class WebClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	WebClient loadBalancingWebClient(WebClient.Builder builder,
			ReactorLoadBalancerExchangeFilterFunction lbFunction) { // <1>
		log.info(
				"registering a default load-balanced " + WebClient.class.getName() + '.');
		return builder.filter(lbFunction).build();
	}

}
