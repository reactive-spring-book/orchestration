package rsb.orchestration.gateway;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.nio.charset.Charset;

@Log4j2
@SpringBootApplication
public class GatewayApplication {

	public static void main(String args[]) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	BrowserLauncher browserLaunchingWebServerInitializedEventListener() {
		return new BrowserLauncher();
	}

}

@Log4j2
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
				"Disable this class if you're running on some other OS besides MacOS and if you don't have Google Chrome installed.!");
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
		return rlb.routes()
				.route(routeSpec -> routeSpec.path("/")
						.filters(fp -> fp.setPath("/guides")).uri("http://spring.io"))
				.build();
	}

}

@Configuration
// @Profile("routes-lb")
class LoadbalancingProxyRouteConfiguration {

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb.routes().route(rs -> rs.path("/").filters(fp -> fp.setPath("/ok"))
				.uri("lb://error-service")).build();

	}

}

// the way i envision the chapter going is that we introduce basic routes, load balanced
// routes, and a bunch of other stuff. when we finally get to the rate limiting bits,
// we'll need to disable this.
@Configuration
class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity http) {
		return http.authorizeExchange(ae -> ae.anyExchange().permitAll()).build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
				.username("jlong").password("pw").roles("USER").build());
	}

}

/*
 *
 * Things to Demonstrate
 *
 * routes predicates uris filters rate limiters modify headers events YAML refreshable
 * routes? custom RouteLocator?
 *
 *
 *
 *
 */