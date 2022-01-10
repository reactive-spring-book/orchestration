package rsb.orchestration.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("routes-filter-simple")
class SimpleProxyFilterRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes()//
				.route(routeSpec -> routeSpec //
						.path("/http") // <1>
						.filters(fs -> fs.setPath("/forms/post")).uri("http://httpbin.org") //
				) //

				.build();
	}

}
