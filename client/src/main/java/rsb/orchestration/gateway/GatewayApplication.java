package rsb.orchestration.gateway;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Log4j2
@SpringBootApplication
public class GatewayApplication {

	public static void main(String args[]) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}

@Log4j2
@Component
class BrowserLauncher implements ApplicationListener<WebServerInitializedEvent> {

	@Override
	@SneakyThrows
	public void onApplicationEvent(WebServerInitializedEvent event) {
		var apps = new File("/Applications");
		var googleChromeAppInstallations = apps
				.listFiles((dir, name) -> name.contains("Google Chrome.app"));
		Assert.state(
				apps.exists() && googleChromeAppInstallations != null
						&& googleChromeAppInstallations.length > 0,
				"Disable this class if you're running on some other OS"
						+ " besides macOS and if you don't have Google Chrome installed on macOS!");
		var port = event.getWebServer().getPort();
		var url = "http://localhost:" + port + "/";
		var exec = Runtime.getRuntime()
				.exec(new String[] { "open", "-n",
						googleChromeAppInstallations[0].getName(), url }, new String[] {},
						apps);
		var error = exec.getErrorStream();
		var statusCode = exec.waitFor();
		var errorString = StreamUtils.copyToString(error, Charset.defaultCharset());
		log.info("the status code is " + statusCode + " and the process output is "
				+ errorString);
	}

}

@Configuration
@Profile("routes-simple")
class SimpleProxyRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes() //
				.route(routeSpec -> routeSpec //
						.path("/")//
						.filters(fp -> fp.setPath("/guides")) //
						.uri("http://spring.io") //
				) //
				.build();
	}

}

@Configuration
@Profile("routes-lb")
class LoadbalancingProxyRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb //
				.routes()//
				.route(rs -> rs //
						.path("/") //
						.filters(fp -> fp.setPath("/ok")) //
						.uri("lb://error-service") //
				)//
				.build();

	}

}

@Log4j2
@Profile("routes-filters")
@Configuration
class ProxyFiltersConfiguration {

	private final Set<String> uids = new HashSet<>();

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes()
				.route(routeSpec -> routeSpec.path("/").filters(fs -> fs.setPath("/ok")//
						.retry(10) //
						.addRequestParameter("uid", UUID.randomUUID().toString())// this
						// has
						// the
						// effect
						// of
						// sending
						// all
						// requests
						// to
						// the
						// downstram
						// endpoint
						// with
						// the
						// same
						// client
						// ID.
						// no
						// reason
						// this
						// couldnt
						// be
						// dynamic.
						// but
						// now
						// seemingly
						// stateless
						// values
						// have
						// state.
						// yay!
						.addRequestHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")//
						.filter((exchange, chain) -> { //
							var uri = exchange.getRequest().getURI();//
							return chain.filter(exchange)
									.doOnSubscribe(sub -> log.info("before: " + uri))
									.doOnEach(signal -> log.info("processing: " + uri))
									.doOnTerminate(() -> log.info("after: " + uri + ". "
											+ "The response status code was "
											+ exchange.getResponse().getStatusCode()
											+ '.'));
						})//
				).uri("lb://error-service")).build();
	}

}

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
				+ List.of(ContextRefreshedEvent.class, RefreshScopeRefreshedEvent.class,
						InstanceRegisteredEvent.class, ParentHeartbeatEvent.class,
						HeartbeatEvent.class).stream().map(Class::getSimpleName)
						.collect(Collectors.joining(","))
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

// the way i envision the chapter going is that we introduce basic routes, load balanced
// routes, and a bunch of other stuff. when we finally get to the rate limiting bits,
// we'll need to customize this.
@Configuration
class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity http) {
		return http.httpBasic(c -> Customizer.withDefaults()) //
				.csrf(ServerHttpSecurity.CsrfSpec::disable) //
				.authorizeExchange(ae -> ae //
						.pathMatchers("/rl").authenticated() //
						.anyExchange().permitAll()) //
				.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
				.username("jlong").password("pw").roles("USER").build());
	}

}

@Profile("rl")
@Configuration
class RateLimiterConfiguration {

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(5, 7);
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes().route(routeSpec -> routeSpec //
				.path("/") //
				.filters(fs -> fs //
						.setPath("/ok") //
						.requestRateLimiter(rl -> rl //
								.setRateLimiter(redisRateLimiter()) //
								.setKeyResolver(new PrincipalNameKeyResolver()) //
				)) //
				.uri("lb://error-service")) //
				.build();
	}

}

/*
 *
 * Things to Demonstrate
 *
 * routes predicates uris filters // rate limiters modify headers // events YAML
 * refreshable routes? custom RouteLocator?
 *
 *
 *
 *
 */