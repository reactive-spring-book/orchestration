package rsb.orchestration.scattergather;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;

import java.util.List;

@Log4j2
@SpringBootApplication
public class ScatterGatherApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ScatterGatherApplication.class, args);
	}

	@Bean
	WebClient client(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> scatterGatherClient(CrmClient client) {
		return event -> {
			Integer[] ids = { 1, 2, 7 };
			Flux<Customer> customerFlux = ensureCached(client.getCustomers(ids));
			Flux<Order> ordersFlux = ensureCached(client.getOrders(ids));
			Flux<CustomerOrders> customerOrdersFlux = customerFlux//
					.flatMap(customer -> {
						Mono<List<Order>> listOfOrdersMono = ordersFlux
								.filter(o -> o.getCustomerId().equals(customer.getId()))
								.collectList();
						Mono<Profile> profileMono = client.getProfile(customer.getId());
						Mono<Customer> customerMono = Mono.just(customer);
						return Flux.zip(customerMono, listOfOrdersMono, profileMono);
					})//
					.map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2(),
							tuple.getT3()));
			customerOrdersFlux.doOnSubscribe(sub -> log.info("starting..."))
					.doOnComplete(() -> log.info("stopping ..."))
					.subscribe(customerOrder -> {
						log.info("---------------");
						log.info(customerOrder.getCustomer().toString());
						log.info(customerOrder.getProfile().toString());
						customerOrder.getOrders().forEach(order -> log.info(
								customerOrder.getCustomer().getId() + ": " + order));
					});
		};
	}

	private <T> Flux<T> ensureCached(Flux<T> in) {
		return in.doOnNext(c -> log.debug("receiving " + c.toString())).cache();
	}

}
