package toy.lsd.reactor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reactor 구성요소 간에 전파되는 key-value 형태의 저장소
 * <p>
 * ThreadLocal과 유사하지만, 스레드가 아니라 Subscriber와 매핑(매 구독 발생 시 Context가 새로 생성됨)
 *
 */
@Slf4j
public class C6Context {

	@SneakyThrows
	private static void ex1_context() {
		/*
		   write는 Context에 하지만, 읽을 때는 ContextView를 통해 읽음
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

	public static void main(String[] args) {
		ex1_context();
	}
}
