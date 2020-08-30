package rsb.orchestration.scattergather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;

@Component
@RequiredArgsConstructor
class CrmClient {

	private final WebClient http;

	// <1>
	Flux<Customer> getCustomers(Integer[] ids) {
		var customersRoot = "http://customer-service/customers?ids="
				+ StringUtils.arrayToDelimitedString(ids, ",");
		return http.get().uri(customersRoot).retrieve().bodyToFlux(Customer.class);
	}

	// <2>
	Flux<Order> getOrders(Integer[] ids) {
		var ordersRoot = "http://order-service/orders?ids="
				+ StringUtils.arrayToDelimitedString(ids, ",");
		return http.get().uri(ordersRoot).retrieve().bodyToFlux(Order.class);
	}

	// <3>
	Mono<Profile> getProfile(Integer customerId) {
		var profilesRoot = "http://profile-service/profiles/{id}";
		return http.get().uri(profilesRoot, customerId).retrieve()
				.bodyToMono(Profile.class);
	}

}
