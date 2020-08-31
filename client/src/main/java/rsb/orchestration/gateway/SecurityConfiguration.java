package rsb.orchestration.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
class SecurityConfiguration {

	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity http) {
		return http //
				.httpBasic(c -> Customizer.withDefaults()) //
				.csrf(ServerHttpSecurity.CsrfSpec::disable) //
				.authorizeExchange(ae -> ae //
						.pathMatchers("/rl").authenticated() // <1>
						.anyExchange().permitAll()) //
				.build();
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
				.username("jlong").password("pw").roles("USER").build());
	}

}
