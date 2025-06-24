package toy.lsd.reactor;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class C2ColdHotSequence {
	@SneakyThrows
	private static void ex1_ColdSequence() {
		// 구독 시점과 상관 없이 시퀀스의 처음부터 전달 받음
		Flux<String> coldFlux = Flux.just("KOREA", "JAPAN", "CHINA").map(String::toLowerCase);

		coldFlux.subscribe(data -> log.info("# Subscriber1 : {}", data));
		Thread.sleep(3000L);
		coldFlux.subscribe(data -> log.info("# Subscriber2 : {}", data));
	}

	@SneakyThrows
	private static void ex2_HotSequence() {
		/* 구독 시점 이후 생성된 데이터만 전달 받음
		 * 최초 구독 시점부터 데이터가 발행되는 warm up,
		 * 구독 여부와 상관없이 데이터가 발행되는 hot 으로 구분할 수 있다.
		 */
		String[] names = {"A", "B", "C", "D", "E", "F"};
		Flux<String> hotFlux = Flux.fromArray(names)
			.delayElements(Duration.ofSeconds(1)) // delayElements의 디폴트 스케줄러: parallel
			.share(); // hot Sequence로 사용하기 위해 원본 Flux를 공유.

		hotFlux.subscribe(data -> log.info("# Subscriber1 : {}", data));
		Thread.sleep(2500L);
		hotFlux.subscribe(data -> log.info("# Subscriber2 : {}", data));
		Thread.sleep(3000L);
		hotFlux.subscribe(data -> log.info("# Subscriber3 : {}", data));
	}

	private static Mono<String> webClientMono() {
		return WebClient.create()
			.get()
			.uri("https://dummyjson.com/test")
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.bodyToMono(String.class);
	}

	@SneakyThrows
	private static void ex3_ColdWebClient() {
		Mono<String> coldWebClient = webClientMono();
		coldWebClient.subscribe(data -> log.info("# Subscriber1 : {}", data));
		Thread.sleep(2000L);
		coldWebClient.subscribe(data -> log.info("# Subscriber2 : {}", data));
		Thread.sleep(2000L);
	}

	@SneakyThrows
	private static void ex3_HotWebClient() {
		Mono<String> hotWebClient = webClientMono().cache(); // hot Mono로 전환하여 모든 구독자들에게 캐싱된 원본 Mono를 전달
		hotWebClient.subscribe(data -> log.info("# Subscriber1 : {}", data));
		Thread.sleep(2000L);
		hotWebClient.subscribe(data -> log.info("# Subscriber2 : {}", data));
		Thread.sleep(2000L);

	}

	public static void main(String[] args) {
		ex1_ColdSequence();
		ex2_HotSequence();
		ex3_ColdWebClient();
		ex3_HotWebClient();
	}
}
