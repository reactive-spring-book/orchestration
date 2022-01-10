package rsb.orchestration.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

@Slf4j
@Profile("events")
@Configuration
class EventsConfiguration {

	@EventListener
	public void refreshRoutesResultEvent(RefreshRoutesResultEvent rre) {
		log.info(rre.getClass().getSimpleName());
		Assert.state(rre.getSource() instanceof CachingRouteLocator,
				() -> "the source must be an instance of " + CachingRouteLocator.class.getName());
		CachingRouteLocator source = (CachingRouteLocator) rre.getSource();
		Flux<Route> routes = source.getRoutes();
		routes.subscribe(
				route -> log.info(route.getClass() + ":" + route.getMetadata().toString() + ":" + route.getFilters()));
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes() //
				.route(routeSpec -> routeSpec //
						.path("/")//
						.filters(fp -> fp.setPath("/guides")) //
						.uri("http://spring.io") //
				) //
				.build();
	}

}
