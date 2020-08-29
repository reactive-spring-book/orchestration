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
