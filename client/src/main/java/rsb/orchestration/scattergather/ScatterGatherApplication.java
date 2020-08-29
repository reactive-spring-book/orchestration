package rsb.orchestration.scattergather;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;
import rsb.orchestration.TimerUtils;

import static rsb.orchestration.TimerUtils.cache;

/**
 * This makes use of several interesting qualities:
 *
 * <ol>
 * <li>zip() makes it easy to process related calls as peers</li>
 * <li>s-c-loadbalancer (and caffeine for caching) work well with Reactive</li>
 * <li>s-c-discovery-client makes it to do client-side loadbalancing - shows one-to-one,
 * one-to-many resolution</li>
 * </ol>
 */
@Log4j2
@SpringBootApplication
public class ScatterGatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScatterGatherApplication.class, args);
	}

}

@Log4j2
@RequiredArgsConstructor
@Component
class ScatterGather implements ApplicationListener<ApplicationReadyEvent> {

	private final CrmClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		var ids = new Integer[] { 1, 2, 7, 5 };
		Flux<Customer> customerFlux = cache(client.getCustomers(ids));
		Flux<Order> ordersFlux = cache(client.getOrders(ids));
		Flux<CustomerOrders> customerOrdersFlux = customerFlux//
				.flatMap(customer -> {
					Flux<Order> filteredOrdersFlux = ordersFlux
							.filter(o -> o.getCustomerId().equals(customer.getId()));
					Mono<Profile> profileMono = client.getProfile(customer.getId());
					Mono<Customer> customerMono = Mono.just(customer);
					return Flux.zip(customerMono, filteredOrdersFlux.collectList(),
							profileMono);
				})//
				.map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2(),
						tuple.getT3()));

		for (var i = 0; i < 5; i++) // it gets faster after successive runs
			run(customerOrdersFlux);
	}

	private void run(Flux<CustomerOrders> customerOrdersFlux) {

		TimerUtils.monitor(customerOrdersFlux)//
				.subscribe(customerOrder -> {
					log.info("---------------");
					log.info(customerOrder.getCustomer());
					log.info(customerOrder.getProfile());
					customerOrder.getOrders().forEach(order -> log
							.info(customerOrder.getCustomer().getId() + ": " + order));
				});
	}

}
