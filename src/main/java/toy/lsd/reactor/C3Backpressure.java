package toy.lsd.reactor;

import java.time.Duration;

import org.reactivestreams.Subscription;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Backpressure 전략
 * 1. 처리량 제어(request 갯수 제어)
 * 2. IGNORE 전략 : Backpressure를 사용하지 않음
 * 3. ERROR 전략 : 데이터가 Downstream 버퍼에 가득 찰 경우, Exception 발생
 * 4. DROP 전략 : 데이터가 Downstream 버퍼에 가득 찰 경우, 추가로 발행되는 데이터는 모두 Drop
 * 5. LATEST 전략 : 데이터가 Downstream 버퍼에 가득 찰 경우, 추가로 발행되는 데이터 중 최신 데이터만 남기고 Drop
 * 6. BUFFER 전략 : 데이터가 Downstream 버퍼에 가득 찰 경우, 버퍼 안의 데이터를 Drop
 * 		6-1. BUFFER DROP_LATEST : 버퍼를 오버플로우 시킨 데이터를 Drop(새로 들어온 데이터)
 * 		6-2. BUFFER DROP_OLDEST : 버퍼 오버플로우 된 경우, 가장 오래된 데이터를 Drop
 */
@Slf4j
public class C3Backpressure {
	private static void ex1_request() {
		Flux.range(1, 5)
			.doOnRequest(data -> log.info("# doOnRequest : {}", data))
			.subscribe(new BaseSubscriber<>() {
				@Override
				protected void hookOnSubscribe(Subscription subscription) {
					request(1); // 1. 처리량 제어
				}

				@SneakyThrows
				@Override
				protected void hookOnNext(Integer value) {
					Thread.sleep(2000L);
					log.info("# hookOnNext : {}", value);
					request(1); // 1. 처리량 제어
				}
			});
	}

	@SneakyThrows
	private static void ex2_ERROR() {
		Flux
			.interval(Duration.ofMillis(1L))
			.onBackpressureError() // 3. ERROR 전략
			.doOnNext(data -> log.info("# doOnNext : {}", data))
			.publishOn(Schedulers.parallel())
			.subscribe(data -> {
					try {
						Thread.sleep(5L);
						log.info("# onNext : {}", data);
					} catch (InterruptedException e) {
					}
				},
				error -> log.error("# onError"));
		Thread.sleep(2000L);
	}

	@SneakyThrows
	private static void ex3_DROP() {
		Flux
			.interval(Duration.ofMillis(1L))
			.onBackpressureDrop(dropped -> log.info("# dropped : {}", dropped)) // 4. DROP 전략
			.publishOn(Schedulers.parallel())
			.subscribe(data -> {
					try {
						Thread.sleep(5L);
						log.info("# onNext : {}", data);
					} catch (InterruptedException e) {
					}
				},
				error -> log.error("# onError", error));
		Thread.sleep(2000L);
	}

	@SneakyThrows
	private static void ex4_LATEST() {
		Flux
			.interval(Duration.ofMillis(1L))
			.onBackpressureLatest() // 5. LATEST 전략
			.publishOn(Schedulers.parallel())
			.subscribe(data -> {
					try {
						Thread.sleep(5L);
						log.info("# onNext : {}", data);
					} catch (InterruptedException e) {
					}
				},
				error -> log.error("# onError", error));
		Thread.sleep(2000L);
	}

	@SneakyThrows
	private static void ex5_BUFFER_DROP_LATEST() {
		Flux
			.interval(Duration.ofMillis(1L))
			.doOnNext(data -> log.info("# emitted by Flux : {}", data))
			.onBackpressureBuffer(2,
				dropped -> log.info("** Overflow & Drop: {} **", dropped),
				BufferOverflowStrategy.DROP_LATEST) // 6-1. BUFFER DROP_LATEST 전략
			.doOnNext(data -> log.info("# emitted by Buffer : {}", data))
			.publishOn(Schedulers.parallel(), false, 1)
			.subscribe(data -> {
					try {
						Thread.sleep(5L);
						log.info("# onNext : {}", data);
					} catch (InterruptedException e) {
					}
				},
				error -> log.error("# onError", error));

		Thread.sleep(2000L);
	}

	@SneakyThrows
	private static void ex6_BUFFER_DROP_OLDEST() {
		Flux.interval(Duration.ofMillis(1L))
			.doOnNext(data -> log.info("# emitted by Flux : {}", data))
			.onBackpressureBuffer(2,
				dropped -> log.info("** Overflow & Drop: {}**", dropped),
					BufferOverflowStrategy.DROP_OLDEST)
			.doOnNext(data -> log.info("# emitted by Buffer : {}", data))
			.publishOn(Schedulers.parallel(), false, 1)
			.subscribe(data -> {
					try {
						Thread.sleep(5L);
						log.info("# onNext : {}", data);
					} catch (InterruptedException e) {
					}
				},
				error -> log.error("# onError", error));
		Thread.sleep(2000L);
	}

	public static void main(String[] args) {
		ex1_request();
		ex2_ERROR();
		ex3_DROP();
		ex4_LATEST();
		ex5_BUFFER_DROP_LATEST();
		ex6_BUFFER_DROP_OLDEST();
	}
}
