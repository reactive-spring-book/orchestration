package rsb.orchestration.hedging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Log4j2
@RequiredArgsConstructor
class HedgingExchangeFilterFunction implements ExchangeFilterFunction {

	private final int timeoutInSeconds = 10;

	private final ReactiveDiscoveryClient reactiveDiscoveryClient;

	private final int maxNodes;

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest,
			ExchangeFunction exchangeFunction) {
		var requestUrl = clientRequest.url();
		var apiName = requestUrl.getHost();

		return this.reactiveDiscoveryClient.getInstances(apiName) //
				.take(maxNodes) //
				.map(si -> buildUriFromServiceInstance(si, requestUrl)) //
				.map(URI::create) //
				.flatMap(uri -> invoke(uri, clientRequest, exchangeFunction)) //
				.timeout(Duration.ofSeconds(timeoutInSeconds)) //
				.take(1) //
				.singleOrEmpty();
	}

	private static String buildUriFromServiceInstance(ServiceInstance server,
			URI requestUrl) {
		var downstreamUrl = (requestUrl.getScheme() + "://" + server.getHost() + ':'
				+ server.getPort() + requestUrl.getPath());
		if (log.isDebugEnabled()) {
			log.debug("the proposed output URI is " + downstreamUrl);
		}
		return downstreamUrl;
	}

	private static Mono<ClientResponse> invoke(URI uri, ClientRequest request,
			ExchangeFunction next) {
		var newRequest = ClientRequest//
				.create(request.method(), uri) //
				.headers(h -> h.addAll(request.headers()))//
				.cookies(c -> c.addAll(request.cookies()))//
				.attributes(a -> a.putAll(request.attributes()))//
				.body(request.body())//
				.build();
		return next//
				.exchange(newRequest)//
				.doOnNext(cr -> log.info("launching " + newRequest.url()));
	}

}
