package rsb.orchestration.assembler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Component
@RequiredArgsConstructor
class Listener {

	private final WebClient http;

	@EventListener(ApplicationReadyEvent.class)
	public void begin() {
		Integer[] ids = { 1, 2, 7 };
		Flux<Customer> customerFlux = ensureCached(getCustomers(ids));
		Flux<Order> ordersFlux = ensureCached(getOrders(ids));
		Flux<CustomerOrders> customerOrdersFlux = customerFlux//
				.flatMap(customer -> {
					Mono<List<Order>> collectList = ordersFlux
							.filter(o -> o.getCustomerId().equals(customer.getId()))
							.collectList();
					return Flux.zip(Mono.just(customer), collectList);
				})//
				.map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2()));

		customerOrdersFlux.subscribe(customerOrder -> {
			log.info("---------------");
			log.info(customerOrder.getCustomer().toString());
			customerOrder.getOrders().forEach(order -> log
					.info(customerOrder.getCustomer().getId() + ": " + order));
		});
	}

	private <T> Flux<T> ensureCached(Flux<T> in) {
		return in.doOnNext(c -> log.debug("receiving " + c.toString())).cache();
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

}