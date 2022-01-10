package rsb.orchestration.reactor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import rsb.orchestration.Order;

@Component
record OrderClient(WebClient http) {

	Flux<Order> getOrders(Integer... ids) {
		var ordersRoot = "http://order-service/orders?ids=" + StringUtils.arrayToDelimitedString(ids, ",");
		return http.get().uri(ordersRoot).retrieve().bodyToFlux(Order.class);
	}

}
