package toy.lsd.reactor;

import java.util.stream.IntStream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
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
	private static void ex2_sinks() {
		int task = 6;

		/**
		 * 1. Sinks.One
		 * 2. Sinks.Many
		 * 		2-1. UnicastSpec
		 * 		2-2. MulticastSpec
		 * 		2-3. MulticastReplaySpec
		 */
		Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

		IntStream.range(1, task)
			.forEach(n -> {
				try {
					new Thread(() -> {
						unicastSink.emitNext(doTasks(n), Sinks.EmitFailureHandler.FAIL_FAST);
						log.info("# emitNext(): {}", n);
					}).start();
					Thread.sleep(100L);
				} catch (InterruptedException e) {}
			});

		unicastSink.asFlux()
			.publishOn(Schedulers.parallel())
			.map(result -> result + " success")
			.doOnNext(n -> log.info("# map(): {}", n))
			.publishOn(Schedulers.parallel())
			.subscribe(data -> log.info("# onNext(): {}", data));

		Thread.sleep(200L);
	}

	public static void main(String[] args) {
		// ex1_create();
		ex2_sinks();
	}

}
