package rsb.orchestration.gateway;

import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("custom-route-locator")
@Configuration
class CustomRouteLocatorConfiguration {

	@Bean
	RouteLocator customRouteLocator(
			SetPathGatewayFilterFactory setPathGatewayFilterFactory) {// <1>
		var setPathGatewayFilter = setPathGatewayFilterFactory
				.apply(config -> config.setTemplate("/guides")); // <2>
		var orderedGatewayFilter = new OrderedGatewayFilter(setPathGatewayFilter, 0);// <3>
		var singleRoute = Route// <4>
				.async() //
				.id("spring-io-guides") //
				.asyncPredicate(serverWebExchange -> Mono.just(true)) //
				.filter(orderedGatewayFilter) //
				.uri("https://spring.io/") //
				.build();

		return () -> Flux.just(singleRoute);// <5>
	}

}
