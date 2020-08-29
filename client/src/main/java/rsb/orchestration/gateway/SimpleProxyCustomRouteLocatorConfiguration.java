package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Configuration
@Profile("proxy-route-locator")
class SimpleProxyCustomRouteLocatorConfiguration {

	@Bean
	RouteLocator proxyAllOfSpringIo() {

		var singleRoute = Route//
				.async() //
				.id("spring-io") //
				.asyncPredicate(serverWebExchange -> Mono.just(true)) //
				.uri("https://spring.io") //
				.build();

		return () -> Flux.just(singleRoute);
	}

}
