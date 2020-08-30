package rsb.orchestration.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

@Configuration
@Profile("predicates")
class PredicateConfiguration {

	@Bean
	RouteLocator predicatesGateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes() //
				.route(routeSpec -> routeSpec //
						.path("/")// <1>
						.uri("http://httpbin.org/") //
				) //
				.route(routeSpec -> routeSpec //
						.header("X-RSB")// <2>
						.uri("http://httpbin.org/") //
				) //
				.route(routeSpec -> routeSpec //
						.query("uid")// <3>
						.uri("http://httpbin.org/") //
				) //
				.route(routeSpec -> routeSpec // <4>
						.asyncPredicate(
								serverWebExchange -> Mono.just(Math.random() > .5))
						.and().path("/test").uri("http://httpbin.org/") //
				) //
				.build();
	}

}
