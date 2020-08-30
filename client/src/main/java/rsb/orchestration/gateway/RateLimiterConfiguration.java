package rsb.orchestration.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("rl")
@Configuration
class RateLimiterConfiguration {

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(5, 7);
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes() //
				.route(routeSpec -> routeSpec //
						.path("/") //
						.filters(fs -> fs //
								.setPath("/ok") //
								.requestRateLimiter(rl -> rl //
										.setRateLimiter(redisRateLimiter()) // <1>
										.setKeyResolver(new PrincipalNameKeyResolver()) // <2>
						)) //
						.uri("lb://error-service")) //
				.build();
	}

}
