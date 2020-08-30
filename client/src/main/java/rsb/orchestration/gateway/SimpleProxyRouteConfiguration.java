package rsb.orchestration.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("routes-simple")
class SimpleProxyRouteConfiguration {

	@Bean // <1>
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes()//
				.route(routeSpec -> routeSpec // <2>
						.alwaysTrue() // <3>
						.uri("https://spring.io") // <4>
				) //
				.build();
	}

}
