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
class FilterConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes() ///
				.route(routeSpec -> routeSpec//
						.path("/")//
						.filters(fs -> fs//
								.setPath("/forms/post")// <1>
								.retry(10) // <2>
								.addRequestParameter("uid", UUID.randomUUID().toString())// <3>
								.addResponseHeader(
										HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")// <4>
								.filter((exchange, chain) -> { // <5>
									var uri = exchange.getRequest().getURI();//
									return chain.filter(exchange) //
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
						.uri("http://httpbin.org"))//
				.build();
	}

}
