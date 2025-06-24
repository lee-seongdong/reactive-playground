package toy.lsd.reactor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Reactor의 비동기 작업을 위한 스레드를 관리하는 구성요소
 * subscribeOn(), publishOn(), parallel().runOn() 을 사용하여 스케줄러를 지정할 수 있음
 */

@Slf4j
public class C5Scheduler {

	@SneakyThrows
	private static void ex1_subscribeOn() {
		/*
		  subscribeOn : 전체 Stream의 스케줄러를 지정함
		  첫번째 이후의 subscribeOn()은 무시됨

		  doOnSubscribe는 subscribe를 호출하는 스레드에서 실행됨 (main 스레드)
		  doOnNext와 onNext subscribeOn()에서 지정한 스케줄러에서 실행됨 (boundedElastic 스레드)
		 */
		Flux.just(1, 3, 5, 7, 9)
			.subscribeOn(Schedulers.boundedElastic())
			.doOnNext(data -> log.info("# doOnNext : {}", data))
			.doOnSubscribe(d -> log.info("# doOnSubscribe : {}", d))
			.subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(500L);
	}

	@SneakyThrows
	private static void ex2_publishOn() {
		/*
		  publishOn : 이후의 연산에 대한 스케줄러를 지정함

		  doOnSubscribe와 doOnNext1는 subscribe를 호출하는 스레드에서 실행됨 (main 스레드)
		  doOnNext2는 publishOn()에서 지정한 스케줄러에서 실행됨 (parallel 스레드)
		  onNext는 publishOn()에서 지정한 스케줄러에서 실행됨 (boundedElastic 스레드)
		 */
		Flux.just(1, 3, 5, 7, 9)
			.doOnSubscribe(d -> log.info("# doOnSubscribe : {}", d))
			.doOnNext(data -> log.info("# doOnNext1 : {}", data))
			.publishOn(Schedulers.parallel())
			.doOnNext(data -> log.info("# doOnNext2 : {}", data))
			.publishOn(Schedulers.boundedElastic())
			.subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(500L);
	}

	@SneakyThrows
	private static void ex3_parallel() {
		/*
		  parallel() : 발행되는 데이터를 물리적인 스레드에 Round Robin 방식으로 분배하는 역할
		  runOn() : 실제로 병렬작업을 수행할 스레드 할당

		 */
		Flux.range(1, 10)
			.parallel(4) // 4개의 스레드로 병렬 처리
			.runOn(Schedulers.parallel())
			.doOnNext(data -> log.info("# onNext : {}", data))
			.subscribe();

		Thread.sleep(500L);
	}

	/*
	 * 스케줄러 종류
	 * 1. immediate : 현재 스레드에서 실행. publishOn을 사용하지 않으면 기본 스케줄러로 사용됨.
	 * 2. single : 하나의 스레드를 재사용하여 작업을 수행
	 * 3. boundedElastic : Blocking I/O 작업(HTTP 요청, DB작업, 파일 I/O)에 적합한 스케줄러로, executorService를 사용하여 스레드풀에서 관리 (기본적으로 CPU 코어 수 * 10 개의 스레드를 생성)
	 * 4. parallel : Non-Blocking I/O 작업에 적합한 스케줄러로, CPU 코어 수 만큼 스레드를 생성하여 병렬 처리
	 * 5. newXXXX : 새로운 커스텀 Scheduler 인스턴스 생성 (newSingle, newParallel, newBoundedElastic 등)
	 */

	@SneakyThrows
	private static void ex4_schedulerImmediate() {
		doTask("task1", Schedulers.immediate()).subscribe(data -> log.info("# onNext: {}", data));
		doTask("task2", Schedulers.immediate()).subscribe(data -> log.info("# onNext: {}", data));

		// Flux.just(1, 3, 5, 7, 9)
		// 	.doOnNext(data -> log.info("# doONNext1 : {}", data))
		// 	.publishOn(Schedulers.parallel())
		// 	.doOnNext(data -> log.info("# doONNext2 : {}", data))
		// 	.publishOn(Schedulers.immediate()) // 현재 스레드에서 실행 (parallel)
		// 	.doOnNext(data -> log.info("# doOnNext3 : {}", data))
		// 	.subscribe(data -> log.info("# onNext : {}", data));

		Thread.sleep(500L);
	}

	@SneakyThrows
	private static void ex4_schedulerSingle() {
		doTask("task1", Schedulers.single()).subscribe(data -> log.info("# onNext: {}", data));
		doTask("task2", Schedulers.single()).subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(500L);
	}

	@SneakyThrows
	private static void ex4_schedulerNewSingle() {
		doTask("task1", Schedulers.newSingle("new-single", true)).subscribe(data -> log.info("# onNext: {}", data));
		doTask("task2", Schedulers.newSingle("new-single", true)).subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(500L);
	}

	private static Flux<Integer> doTask(String taskName, Scheduler scheduler) {
		return Flux.just(1, 3, 5, 7, 9)
			.publishOn(scheduler)
			.doOnNext(data -> log.info("# {} doOnNext1 : {}", taskName, data))
			.map(data -> data * 10)
			.doOnNext(data -> log.info("# {} doOnNext2 : {}", taskName, data));
	}

	public static void main(String[] args) {
		// ex1_subscribeOn();
		// ex2_publishOn();
		// ex3_parallel();

		// ex4_schedulerImmediate();
		// ex4_schedulerSingle();
		// ex4_schedulerNewSingle();
	}
}
