package rsb.orchestration.gateway;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This should automatically register GW routes based on the routes discovered in the
 * service registry. There's also a property i could use instead of registering this bean!
 * The property is more natural to show! Show that first.
 */
@Configuration
@Profile("discovery-routes")
class DiscoveryClientRoutesConfiguration {

	@Bean
    DiscoveryClientRouteDefinitionLocator discoveryClientRouteDefinitionLocator(
            ReactiveDiscoveryClient dc, DiscoveryLocatorProperties properties) {
		return new DiscoveryClientRouteDefinitionLocator(dc, properties);
	}

}
