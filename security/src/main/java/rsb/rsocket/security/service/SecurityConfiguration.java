package rsb.rsocket.security.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

@Configuration
class SecurityConfiguration {

	// <1>
	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(
				User.withDefaultPasswordEncoder().username("rwinch").password("pw")
						.roles("ADMIN", "USER").build(),
				User.withDefaultPasswordEncoder().username("jlong").password("pw")
						.roles("USER").build());
	}

	// <2>
	@Bean
	PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
		return security//
				.simpleAuthentication(Customizer.withDefaults())//
				.build();
	}

	// <3>
	@Bean
	RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
		var mh = new RSocketMessageHandler();
		mh.getArgumentResolverConfigurer()
				.addCustomResolver(new AuthenticationPrincipalArgumentResolver());
		mh.setRSocketStrategies(strategies);
		return mh;
	}

}
