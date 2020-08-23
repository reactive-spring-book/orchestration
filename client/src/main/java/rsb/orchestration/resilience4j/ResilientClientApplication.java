package rsb.orchestration.resilience4j;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@SpringBootApplication
public class ResilientClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResilientClientApplication.class, args);
	}

	@Bean
	WebClient loadBalancedWebClient(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder();
	}

}