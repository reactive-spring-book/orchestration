package rsb.rsocket.routing.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SignalType;
import rsb.rsocket.routing.Customer;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.rSocketRequester//
				.route("customers.{id}", 1)//
				.retrieveMono(Customer.class)//
				.doOnNext(log::info)//
				.doFinally(this::line)//
				.subscribe();

		this.rSocketRequester//
				.route("customers")//
				.retrieveFlux(Customer.class)//
				.doOnNext(log::info)//
				.doFinally(this::line)//
				.subscribe();
	}

	private void line(SignalType st) {
		log.info("---------------------");
	}

}
