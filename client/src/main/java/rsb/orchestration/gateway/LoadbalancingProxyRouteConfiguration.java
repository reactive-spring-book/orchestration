package rsb.orchestration.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("routes-lb")
class LoadbalancingProxyRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes()//
				.route(rs -> rs.alwaysTrue().uri("lb://error-service"))//
				.build();

	}

}
