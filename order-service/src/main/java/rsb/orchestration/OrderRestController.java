package rsb.orchestration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequestMapping("/orders")
@RestController
class OrderRestController {

	// customerId -> orders
	private final Map<Integer, List<Order>> orders = //
			IntStream//
					.range(0, 10)//
					.boxed()//
					.map(id -> Map.entry(id, new CopyOnWriteArrayList<Order>()))
					.collect(Collectors.toConcurrentMap(Map.Entry::getKey, e -> {
						var listOfOrders = e.getValue();
						var max = (int) (Math.random() * 10);
						if (max < 1) {
							max = 1;
						}
						for (var i = 0; i < max; i++) {
							listOfOrders.add(
									new Order(UUID.randomUUID().toString(), e.getKey()));
						}
						return listOfOrders;
					}));

	@GetMapping
	Flux<Order> orders(@RequestParam(required = false) Integer[] ids) {
		var customerStream = this.orders.keySet().stream();
		var includedCustomerIds = Arrays.asList(ids);
		var orderStream = customerStream.filter(includedCustomerIds::contains)//
				.flatMap(id -> this.orders.get(id).stream());
		return Flux.fromStream(orderStream);
	}

}
