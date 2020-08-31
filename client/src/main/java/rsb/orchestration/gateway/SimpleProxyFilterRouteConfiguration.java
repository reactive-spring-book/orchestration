package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Log4j2
@Configuration
@Profile("routes-filter-simple")
class SimpleProxyFilterRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes()//
				.route(routeSpec -> routeSpec //
						.path("/http") // <1>
						.filters(fs -> fs.setPath("/forms/post"))
						.uri("http://httpbin.org") //
				) //

				.build();
	}

}
