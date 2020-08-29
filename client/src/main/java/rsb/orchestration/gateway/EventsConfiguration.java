package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Log4j2
@Profile("events")
@Configuration
class EventsConfiguration {

	private final Set<String> events = new ConcurrentSkipListSet<>();

	private final Object monitor = new Object();

	@EventListener
	public void listenForGatewayEvents(ApplicationEvent event) {
		var clazzName = event.getClass().getSimpleName();
		synchronized (this.monitor) {
			if (!this.events.contains(clazzName)) {
				this.events.add(clazzName);
				if (log.isDebugEnabled()) {
					log.debug("event: " + clazzName);
				}
			}
		}
	}

	@EventListener
	public void refreshRoutesEvent(RefreshRoutesEvent rre) {
		log.info(rre.getClass().getSimpleName());
		Object source = rre.getSource();
		log.info("this event is published first whenever anything "
				+ "in the world could change our world view of available service registry routes. It is itself a "
				+ "sort of meta event that tells us when any of several useful other events are published including ("
				+ List.of(//
						ContextRefreshedEvent.class, RefreshScopeRefreshedEvent.class,
						InstanceRegisteredEvent.class, ParentHeartbeatEvent.class,
						HeartbeatEvent.class //
				).stream().map(Class::getSimpleName).collect(Collectors.joining(","))
				+ ")");
		Assert.state(source instanceof RouteRefreshListener);
	}

	// the component that publishes the RefreshRoutesResultEvent itself listens for
	// RefreshRoutesEvents.
	@EventListener
	public void refreshRoutesResultEvent(RefreshRoutesResultEvent rre) {
		log.info(rre.getClass().getSimpleName());
		Assert.state(rre.getSource() instanceof CachingRouteLocator);
		CachingRouteLocator source = (CachingRouteLocator) rre.getSource();
		Flux<Route> routes = source.getRoutes();
		routes.subscribe(route -> log.info(route.getClass() + ":"
				+ route.getMetadata().toString() + ":" + route.getFilters()));
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
