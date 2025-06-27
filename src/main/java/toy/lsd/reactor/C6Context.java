package toy.lsd.reactor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

/**
 * Reactor 구성요소 간에 전파되는 key-value 형태의 저장소
 * ThreadLocal과 유사하지만, 스레드가 아니라 Subscriber와 매핑(매 구독 발생 시 Context가 새로 생성됨)
 *
 */
@Slf4j
public class C6Context {

	@SneakyThrows
	private static void ex1_context() {
		/*
		   1. 구독이 발생하면 context에 데이터 저장됨 (Steve, Jobs)
		   2. 원본 데이터 소스레벨에서 ContextView를 통해 저장된 데이터 읽어옴 (deferContextual())
		   3. Operator 체이닝 중간에서 ContextView를 통해 저장된 데이터 읽어옴 (transformDeferredContextual())
		 */
		Mono.deferContextual(contextView ->
				Mono.just("Hello " + contextView.get("firstName"))
					.doOnNext(data -> log.info(" # just doOnNext : {}", data)))
			.subscribeOn(Schedulers.boundedElastic())
			.publishOn(Schedulers.parallel())
			.transformDeferredContextual((mono, contextView) -> mono.map(data -> data + " " + contextView.get("lastName")))
			.contextWrite(context -> context.put("lastName", "Jobs"))
			.contextWrite(context -> context.put("firstName", "Steve"))
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(100L);
	}

	@SneakyThrows
	private static void ex2_contextApi() {
		String key1 = "k1";
		String key2 = "k2";
		String key3 = "k3";
		Mono.deferContextual(contextView -> Mono.just(contextView.get(key1) + "," + contextView.get(key2) + "," + contextView.get(key3)))
			.publishOn(Schedulers.parallel())
			.contextWrite(context -> context.putAll(Context.of(key1, "hello", key2, "world").readOnly()))
			.contextWrite(context -> context.put(key3, "reactor"))
			.subscribe(data -> log.info("# onNext: {}", data));
		Thread.sleep(100L);
	}

	@SneakyThrows
	private static void ex3_contextViewApi() {
		String key1 = "k1";
		String key2 = "k2";
		String key3 = "k3";

		Mono.deferContextual(contextView -> Mono.just(contextView.get(key1) + ", " + contextView.getOrEmpty(key2).orElse("empty") + ", " + contextView.getOrDefault(key3, "default")))
			.publishOn(Schedulers.parallel())
			.contextWrite(context -> context.put(key1, "hello"))
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(100L);
	}

	@SneakyThrows
	private static void ex4_contextShare() {
		String key1 = "k1";

		// 다른 구독끼리는 Context 공유 불가능
		Mono<String> mono = Mono.deferContextual(contextView -> Mono.just("Hello! " + contextView.get(key1)))
			.publishOn(Schedulers.parallel());

		mono.contextWrite(context -> context.put(key1, "Tom"))
			.subscribe(data -> log.info("# onNext: {}", data));

		mono.contextWrite(context -> context.put(key1, "Kim"))
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(100L);
	}

	@SneakyThrows
	private static void ex5_contextOverwrite() {
		String key1 = "k1";
		String key2 = "k2";

		/*
			Operator 체인은 아래에서 위로 전파됨. 그래서 contextWrite는 Operator 체인의 마지막에 두어야한다
			1. 구독 발생
			2. context에 key1 write
			3. context에서 key2 read (EMPTY)
			4. context에 key2 write
			5. context를 읽어 데이터 생성
		 */
		Mono.deferContextual(contextView -> Mono.just("Hello! " + contextView.get(key1)))
			.contextWrite(context -> context.put(key2, "Tom"))
			.transformDeferredContextual((mono, contextView) -> mono.map(data -> data + " " + contextView.getOrDefault(key2, "kim")))
			.contextWrite(context -> context.put(key1, "World"))
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	public static void main(String[] args) {
		// ex1_context();
		// ex2_contextApi();
		// ex3_contextViewApi();
		// ex4_contextShare();
		ex5_contextOverwrite();
	}
}
