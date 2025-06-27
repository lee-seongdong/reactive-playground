package toy.lsd.reactor;

import java.util.concurrent.CountDownLatch;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * 리액티브 스트림즈 : 비동기 스트림 처리를 위한 표준 API 스펙
 * - 비동기 및 논블로킹
 * - 백프레셔를 통해 데이터 흐름 제어
 * - 주요 표준 인터페이스 : Publisher, Subscriber, Subscription, Processor
 *
 * 주요 구현체
 * - Reactor (Spring WebFlux에서 사용)
 * - RxJava (Android 개발 시 주로 사용)
 * - Flow API (Java 9+ 내장)
 *
 */
public class C1Basic {
	private static void ex1_Mono() {
		Mono.just("Hello World!")
			.subscribe(System.out::println);

		Mono.empty()
			.subscribe(
				data -> System.out.println("onNext" + data), // onNext
				System.out::println, // onError
				() -> System.out.println("onComplete") // onComplete
			);
	}

	private static void ex2_Flux() {
		Flux.just(6, 9, 13)
			.map(num -> num % 2)
			.subscribe(System.out::println);

		Flux.fromArray(new Integer[] {6, 9, 12, 15})
			.filter(num -> num % 2 == 0)
			.map(num -> num + 1)
			.subscribe(System.out::println);

		String data1 = "Hello World!";
		String data2 = null;
		Flux<String> flux = Mono.just(data1)
			.concatWith(Mono.justOrEmpty(data2));
		flux.subscribe(System.out::println);

		Flux.concat(
				Flux.just("Jan", "Feb", "Mar"),
				Flux.just("Apr", "May", "Jun"),
				Flux.just("Jul", "Aug", "Sep"))
			.collectList()
			.subscribe(months -> System.out.println(months));
	}

	@SneakyThrows
	private static void ex3_WebClient() {
		CountDownLatch latch = new CountDownLatch(1);

		WebClient
			.create()
			.get()
			.uri("https://dummyjson.com/test")
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.bodyToMono(String.class)
			.subscribe(
				data -> System.out.println("onNext" + data),
				System.out::println,
				latch::countDown
			);

		latch.await();
	}

	public static void main(String[] args) {
		// ex1_Mono();
		// ex2_Flux();
		// ex3_WebClient();
	}
}
