package rsb.orchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
class WebClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	WebClient loadBalancingWebClient(WebClient.Builder builder, ReactorLoadBalancerExchangeFilterFunction lbFunction) { // <1>
		log.info("registering a default load-balanced " + WebClient.class.getName() + '.');
		return builder.filter(lbFunction).build();
	}

}
