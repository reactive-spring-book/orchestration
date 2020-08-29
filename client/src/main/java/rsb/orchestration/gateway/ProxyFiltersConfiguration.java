package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Profile("routes-filters")
@Configuration
class ProxyFiltersConfiguration {

	private final Set<String> uids = new HashSet<>();

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes() ///
				.route(routeSpec -> routeSpec//
						.path("/")//
						.filters(fs -> fs//
								.setPath("/ok")//
								.retry(10) //
								.addRequestParameter("uid", UUID.randomUUID().toString())// this
								.addRequestHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
										"*")//
								.filter((exchange, chain) -> { //
									var uri = exchange.getRequest().getURI();//
									return chain.filter(exchange)
											.doOnSubscribe(
													sub -> log.info("before: " + uri))
											.doOnEach(signal -> log
													.info("processing: " + uri))
											.doOnTerminate(() -> log.info("after: " + uri
													+ ". "
													+ "The response status code was "
													+ exchange.getResponse()
															.getStatusCode()
													+ '.'));
								})//
						)//
						.uri("lb://error-service"))//
				.build();
	}

}
