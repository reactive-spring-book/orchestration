package rsb.orchestration.scattergather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.Customer;
import rsb.orchestration.Order;
import rsb.orchestration.TimerUtils;

@Slf4j
@Component
record ScatterGather(CrmClient client) {

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		var ids = new Integer[] { 1, 2, 7, 5 }; // <1>
		// <2>
		Flux<Customer> customerFlux = TimerUtils.cache(client.getCustomers(ids));
		Flux<Order> ordersFlux = TimerUtils.cache(client.getOrders(ids));
		Flux<CustomerOrders> customerOrdersFlux = customerFlux//
				.flatMap(customer -> { // <3>

					// <4>
					var monoOfListOfOrders = ordersFlux //
							.filter(o -> o.customerId().equals(customer.id()))//
							.collectList();

					// <5>
					var profileMono = client.getProfile(customer.id());

					// <6>
					var customerMono = Mono.just(customer);

					// <7>
					return Flux.zip(customerMono, monoOfListOfOrders, profileMono);
				})// <8>
				.map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2(), tuple.getT3()));

		for (var i = 0; i < 5; i++) // <9>
			run(customerOrdersFlux);
	}

	private void run(Flux<CustomerOrders> customerOrdersFlux) {
		TimerUtils //
				.monitor(customerOrdersFlux)//
				.subscribe(customerOrder -> {
					log.info("---------------");
					log.info(customerOrder.customer().toString());
					log.info(customerOrder.profile().toString());
					customerOrder.orders().forEach(order -> log.info(customerOrder.customer().id() + ": " + order));
				});
	}

}
