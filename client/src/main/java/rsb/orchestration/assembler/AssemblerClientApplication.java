package rsb.orchestration.assembler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class AssemblerClientApplication {

	public static void main(String args[]) throws Exception {
		SpringApplication.run(AssemblerClientApplication.class, args);
		System.in.read();
	}

	@Bean
	WebClient client(WebClient.Builder builder) {
		return builder.build();
	}

}

@Component
@RequiredArgsConstructor
class Listener {

	private final WebClient http;

	@EventListener(ApplicationReadyEvent.class)
	public void begin() {
		Integer[] ids = new Integer[] { 1, 7, 2 };
		Flux<Customer> customerFlux = getCustomers(ids).cache();
		Flux<Order> ordersFlux = getOrders(ids).cache();
		Flux<CustomerOrders> customerOrdersFlux = customerFlux.flatMap(customer -> {
			Mono<List<Order>> collectList = ordersFlux
					.filter(o -> o.getCustomerId().equals(customer.getId()))
					.collectList();
			return Flux.zip(Mono.just(customer), collectList);
		}).map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2()));

		customerOrdersFlux.subscribe(tuple -> System.out
				.println(tuple.getCustomer().toString() + "=" + tuple.getOrders()));

		// regular
		// .publishOn(scheduler)
		// .subscribeOn(scheduler)
		// .subscribe(System.out::println);
	}

	Flux<Order> find(Customer c, Flux<Order> orders) {
		return orders.filter(o -> o.getCustomerId().equals(c.getId()));
	}

	private Flux<Order> getOrders(Integer[] ids) {
		var ordersRoot = "http://localhost:8082/orders?ids=" + buildStringForIds(ids);
		return http.get().uri(ordersRoot).retrieve().bodyToFlux(Order.class);
	}

	private Flux<Customer> getCustomers(Integer[] ids) {
		var customersRoot = "http://localhost:8081/customers?ids="
				+ buildStringForIds(ids);
		return http.get().uri(customersRoot).retrieve().bodyToFlux(Customer.class);
	}

	private String buildStringForIds(Integer[] ids) {
		return Arrays.stream(ids).map(id -> Integer.toString(id))
				.collect(Collectors.joining(","));
	}

}

@Data
@RequiredArgsConstructor
class CustomerOrders {

	private final Customer customer;

	private final Collection<Order> orders;

	// private final Collection<Order> orders;

}

@Data
@RequiredArgsConstructor
class ReactiveCustomerOrders {

	private final Customer customer;

	private final Flux<Order> orders;

	// private final Collection<Order> orders;

}
