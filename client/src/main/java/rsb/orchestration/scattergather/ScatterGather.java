package rsb.orchestration.scattergather;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.Profile;
import rsb.orchestration.TimerUtils;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Component
class ScatterGather implements ApplicationListener<ApplicationReadyEvent> {

	private final CrmClient client;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		var ids = new Integer[] { 1, 2, 7, 5 }; // <1>
		// <2>
		Flux<Customer> customerFlux = TimerUtils.cache(client.getCustomers(ids));
		Flux<Order> ordersFlux = TimerUtils.cache(client.getOrders(ids));
		Flux<CustomerOrders> customerOrdersFlux = customerFlux//
				.flatMap(customer -> { // <3>

					// <4>
					Mono<List<Order>> filteredOrdersFlux = ordersFlux //
							.filter(o -> o.getCustomerId().equals(customer.getId()))//
							.collectList();

					// <5>
					Mono<Profile> profileMono = client.getProfile(customer.getId());

					// <6>
					Mono<Customer> customerMono = Mono.just(customer);

					// <7>
					return Flux.zip(customerMono, filteredOrdersFlux, profileMono);
				})// <8>
				.map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2(),
						tuple.getT3()));

		for (var i = 0; i < 5; i++) // <9>
			run(customerOrdersFlux);
	}

	private void run(Flux<CustomerOrders> customerOrdersFlux) {
		TimerUtils //
				.monitor(customerOrdersFlux)//
				.subscribe(customerOrder -> {
					log.info("---------------");
					log.info(customerOrder.getCustomer());
					log.info(customerOrder.getProfile());
					customerOrder.getOrders().forEach(order -> log
							.info(customerOrder.getCustomer().getId() + ": " + order));
				});
	}

}
