package rsb.orchestration;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class TimerUtils {

	// <1>
	public static <T> Mono<T> cache(Mono<T> cache) {
		return cache.doOnNext(c -> log.debug("receiving " + c.toString())).cache();
	}

	public static <T> Flux<T> cache(Flux<T> cache) {
		return cache.doOnNext(c -> log.debug("receiving " + c.toString())).cache();
	}

	// <2>
	public static <T> Mono<T> monitor(Mono<T> configMono) {
		var start = new AtomicLong();
		return configMono//
				.doOnError(exception -> log.error("oops!", exception))//
				.doOnSubscribe((subscription) -> start.set(System.currentTimeMillis())) //
				.doOnNext((greeting) -> log.info("total time: {}", System.currentTimeMillis() - start.get()));
	}

	public static <T> Flux<T> monitor(Flux<T> configMono) {
		var start = new AtomicLong();
		return configMono//
				.doOnError(exception -> log.error("oops!", exception))//
				.doOnSubscribe((subscription) -> start.set(System.currentTimeMillis())) //
				.doOnNext((greeting) -> log.info("total time: {}", System.currentTimeMillis() - start.get()));
	}

}
