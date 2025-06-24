package toy.lsd.reactor;

import java.util.stream.IntStream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Publiser와 Subscriber의 기능을 모두 지닌 구성요소 (Processor)
 * Processor의 기능을 개선한 구성요소로, 3.4.0부터 지원함. (Processor는 3.5.0부터 완전 제거)
 *
 * Sinks를 사용하면 프로그래밍을 통해 명시적으로 Signal을 전송할 수 있음 (Thread safe 하다)
 * (generate, create Operator도 sinal을 전송하는데 사용하지만, 싱글 스레드 기반에서 사용)
 */
@Slf4j
public class C4Sinks {
	private static String doTasks(int taskNumber) {
		return "task " + taskNumber + " result";
	}

	@SneakyThrows
	private static void ex1_create() {
		int task = 6;

		Flux.create((FluxSink<String> sink) -> {
				IntStream.range(1, task)
					.forEach(i -> sink.next(doTasks(i)));
			})
			.subscribeOn(Schedulers.boundedElastic())
			.doOnNext(n -> log.info("# create(): {}", n))
			.publishOn(Schedulers.parallel())
			.map(result -> result + " success")
			.doOnNext(n -> log.info("# map(): {}", n))
			.publishOn(Schedulers.parallel())
			.subscribe(data -> log.info("# onNext(): {}", data));

		Thread.sleep(500L);
	}

	@SneakyThrows
	private static void ex1_sinks() {
		int task = 6;

		Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

		IntStream.range(1, task)
			.forEach(n -> {
				try {
					new Thread(() -> {
						unicastSink.emitNext(doTasks(n), Sinks.EmitFailureHandler.FAIL_FAST);
						log.info("# emitNext(): {}", n);
					}).start();
					Thread.sleep(100L);
				} catch (InterruptedException e) {
				}
			});

		unicastSink.asFlux()
			.publishOn(Schedulers.parallel())
			.map(result -> result + " success")
			.doOnNext(n -> log.info("# map(): {}", n))
			.publishOn(Schedulers.parallel())
			.subscribe(data -> log.info("# onNext(): {}", data));

		Thread.sleep(200L);
	}

	/**
	 * 1. Sinks.One
	 * 2. Sinks.Many
	 * 		2-1. UnicastSpec
	 * 		2-2. MulticastSpec
	 * 		2-3. MulticastReplaySpec
	 */
	private static void ex2_sinksOne() {
		Sinks.One<String> oneSink = Sinks.one();

		oneSink.emitValue("Hello reactor", Sinks.EmitFailureHandler.FAIL_FAST);
		oneSink.emitValue("Hi reactor", Sinks.EmitFailureHandler.FAIL_FAST); // 이후 발행되는 데이터들은 모두 Drop된다.

		Mono<String> mono = oneSink.asMono();

		mono.subscribe(n -> log.info("# Subscriber1: {}", n));
		mono.subscribe(n -> log.info("# Subscriber2: {}", n));
	}

	private static void ex2_sinksManyUnicast() {
		Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
		Flux<Integer> flux = unicastSink.asFlux();

		unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
		unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

		flux.subscribe(data -> log.info("# Subscriber1: {}", data));
		unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

		// flux.subscribe(data -> log.info("# Subscriber2: {}", data)); // unicastSink는 Subscriber가 하나만 허용되므로, Exception 발생
	}

	private static void ex2_sinksManyMulticast() {
		Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer(); // warm up 방식의 Hot Sequence
		Flux<Integer> flux = multicastSink.asFlux();

		multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
		multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

		flux.subscribe(data -> log.info("# Subscriber1: {}", data));

		// 멀티캐스트 Sink는 여러 Subscriber를 허용함
		// onBackpressureBuffer()는 warm up 방식의 hot Sequence로, Subscriber2는 구독 이후 발행된 데이터만 받는다.
		flux.subscribe(data -> log.info("# Subscriber2: {}", data));

		multicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	private static void ex2_sinksManyMulticastReplay() {
		Sinks.Many<Integer> multicastReplaySink = Sinks.many().replay().limit(2); // 최근 2개의 데이터만 저장. (all()은 모든 데이터를 저장)
		Flux<Integer> flux = multicastReplaySink.asFlux();

		multicastReplaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
		multicastReplaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
		multicastReplaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

		flux.subscribe(data -> log.info("# Subscriber1: {}", data));

		multicastReplaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);

		flux.subscribe(data -> log.info("# Subscriber2: {}", data));
	}

	public static void main(String[] args) {
		// ex1_create();
		// ex1_sinks();

		ex2_sinksOne();
		// ex2_sinksManyUnicast();
		// ex2_sinksManyMulticast();
		// ex2_sinksManyMulticastReplay();
	}
}
