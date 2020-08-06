package rsb.rsocket.security.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.security.GreetingRequest;
import rsb.rsocket.security.GreetingResponse;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
public class SecurityApplication {

	public static void main(String args[]) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(SecurityApplication.class, args);
	}

}

@Configuration
class SecurityConfiguration {

	@Bean
	RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
		var mh = new RSocketMessageHandler();
		mh.getArgumentResolverConfigurer()
				.addCustomResolver(new AuthenticationPrincipalArgumentResolver());
		mh.setRSocketStrategies(strategies);
		return mh;
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(
				User.withDefaultPasswordEncoder().username("rwinch").password("pw")
						.roles("ADMIN", "USER").build(),
				User.withDefaultPasswordEncoder().username("jlong").password("pw")
						.roles("USER").build());
	}

	@Bean
	PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
		return security.simpleAuthentication(Customizer.withDefaults()).build();
	}

}

@Controller
class GreetingsController {

	@MessageMapping("greetings")
	Flux<GreetingResponse> greet(@AuthenticationPrincipal Mono<UserDetails> user) {
		return user.map(UserDetails::getUsername).map(GreetingRequest::new)
				.flatMapMany(this::greet);
	}

	private Flux<GreetingResponse> greet(GreetingRequest request) {
		return Flux
				.fromStream(Stream.generate(
						() -> new GreetingResponse("Hello, " + request.getName() + "!")))
				.delayElements(Duration.ofSeconds(1));
	}

}