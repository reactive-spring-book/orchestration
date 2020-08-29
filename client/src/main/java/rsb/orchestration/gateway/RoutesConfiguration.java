package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Profile("discovery-routes")
@Configuration
class RoutesConfiguration {

	@Bean
    RouteLocator customGatewayRouteLocator(
			SetPathGatewayFilterFactory setPathGatewayFilterFactory) {

		var setPathGatewayFilter = setPathGatewayFilterFactory
				.apply(config -> config.setTemplate("/guides"));

		// OrderedGatewayFilter *must* wrap GatewayFilters!
		var orderedGatewayFilter = new OrderedGatewayFilter(setPathGatewayFilter, 0);

		var singleRoute = Route//
				.async() //
				.id("spring-io-guides")
				.asyncPredicate(serverWebExchange -> Mono.just(true))
				.filter(orderedGatewayFilter).uri("https://spring.io/").build();

		return () -> Flux.just(singleRoute);
	}

}
