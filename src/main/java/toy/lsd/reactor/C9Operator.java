package toy.lsd.reactor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import org.reactivestreams.Subscription;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

/**
 * 데이터 스트림을 처리하기 위한 연산자
 */
@Slf4j
public class C9Operator {
	/// 1. 생성 Operator
	private static void ex1_justOrEmpty() {
		Mono.justOrEmpty(null) // null이 들어오면 예외 발생하지 complete signal 전송.
			.subscribe(data -> System.out.println("Received: " + data),
				error -> System.err.println("Error: " + error.getMessage()),
				() -> System.out.println("onComplete"));
	}

	private static void ex2_fromXXX() {
		Flux.fromArray(new String[] {"Hello", "Reactor"})
			.subscribe(data -> System.out.println("onNext: " + data));

		Flux.fromIterable(Arrays.asList(1, 2, 3))
			.subscribe(data -> System.out.println("onNext: " + data));

		Flux.fromStream(Stream.of("Hello", "Reactor"))
			.subscribe(data -> System.out.println("onNext: " + data));
	}

	private static void ex3_range() {
		Flux.range(5, 3) // 5부터 3개의 숫자를 생성 (5, 6, 7)
			.subscribe(data -> System.out.println("onNext: " + data));
	}

	@SneakyThrows
	private static void ex4_defer1() {
		/*
		 * defer()는 실행을 지연 시키기 위한 연산자
		 * 선언한 시점에 실행 되는 것이 아니라, 구독 시점에 실행됨.
		 */
		log.info("# start: {}", LocalDateTime.now());

		Mono<LocalDateTime> justMono = Mono.just(LocalDateTime.now()); // 이때 LocalDateTime.now()가 호출되어 현재 시간이 저장됨. (just는 hot sequence로, 구독여부와 상관없이 데이터 emit. 구독 발생 시 replay)
		Mono<LocalDateTime> deferMono = Mono.defer(() -> Mono.just(LocalDateTime.now()));

		Thread.sleep(2000L);

		justMono.subscribe(data -> log.info("# onNext just1: " + data));
		deferMono.subscribe(data -> log.info("# onNext defer1: " + data)); // 이때 defer의 LocalDateTime.now()가 호출되어 현재 시간이 저장됨.

		Thread.sleep(2000L);

		justMono.subscribe(data -> log.info("# onNext just2: " + data));
		deferMono.subscribe(data -> log.info("# onNext defer2: " + data)); // 이때 defer의 LocalDateTime.now()가 호출되어 현재 시간이 저장됨.
	}

	private static Mono<String> sayDefault() {
		log.info("# sayDefault");
		return Mono.just("Hi");
	}

	@SneakyThrows
	private static void ex5_defer2() {
		log.info("# start: {}", LocalDateTime.now());
		Mono.just("Hello")
			.delayElement(Duration.ofSeconds(2))
			.switchIfEmpty(sayDefault()) // operator chaining 에서 메소드 호출 결과를 사용하기 때문에, 실제 데이터 흐름과 상관없이 메소드가 호출된다.
			.switchIfEmpty(Mono.defer(C9Operator::sayDefault)) // 구독 발생 시점에 실제 데이터 흐름에 따라 메소드가 호출된다.
			.subscribe(data -> log.info("# onNext: " + data));
		Thread.sleep(3500L);
	}

	private static void ex6_using() {
		/*
		 * using()은 리소스를 관리하기 위한 연산자
		 * 리소스가 필요할 때 생성, 리소스로부터 데이터 emit, 구독이 종료 시 리소스 해제
		 */

		Path path = Paths.get("src/main/resources/test.txt");

		Flux.using(
				() -> Files.lines(path), // 읽어올 리소스 생성
				p -> Flux.fromStream(p), // 리소스를 사용하여 데이터를 emit
				p -> {
					log.info("Closing resource: " + p.toString());
					p.close();
				}) // 리소스 해제 (complete signal 발생 시)
			.subscribe(data -> log.info("# onNext: " + data));
	}

	private static void ex7_generate1() {
		/*
		 * generate()는 프로그래밍 방식으로 데이터를 생성하기 위한 연산자.
		 * 특히 하나의 데이터를 동기적 생성할 때 사용.
		 */

		Flux.generate(() -> 0, // 초기 state supply
				(state, sink) -> {
					sink.next(state);
					// sink.next("tt"); // 에러발생. generate에 사용하는 sink는 SynchronousSink로, 최대 하나의 데이터만 emit 가능하다.
					if (state == 10) {
						sink.complete();
					}
					return ++state; // 다음 상태를 반환
				})
			.subscribe(data -> log.info("# onNext: " + data));
	}

	private static void ex8_generate2() {
		int dan = 3;

		Flux.generate(() -> Tuples.of(dan, 1), // 초기 state supply
				(state, sink) -> {
					sink.next(state.getT1() + " * " + state.getT2() + " = " + state.getT1() * state.getT2());
					if (state.getT2() == 9) {
						sink.complete();
					}
					return Tuples.of(state.getT1(), state.getT2() + 1);
				},
				state -> log.info("# 구구단 {}단 종료. {}", state.getT1(), state.getT2())) // 마지막 state consume
			.subscribe(data -> log.info("# onNext: " + data));
	}

	private static int SIZE = 0;
	private static int COUNT = -1;
	private static List<Integer> DATA_SOURCE = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	private static void ex9_create() {
		/*
		 * create()는 프로그래밍 방식으로 데이터를 생성하기 위한 연산자.
		 * 특히 여러개의 데이터를 비동기적으로 생성할 때 사용.
		 */
		log.info("# start create");
		Flux.create((FluxSink<Integer> sink) -> {
			// request 발생 시 데이터 emit하는 pull 방식
			sink.onRequest(n -> {
				try {
					Thread.sleep(1000);
					for (int i = 0; i < n; i++) {
						if (COUNT >= 9) {
							sink.complete();
						} else {
							COUNT++;
							sink.next(DATA_SOURCE.get(COUNT));
						}
					}
				} catch (Exception e) {
				}
			});

			sink.onDispose(() -> log.info("# clean up"));
		}).subscribe(new BaseSubscriber<>() {
			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				request(2);
			}

			@Override
			protected void hookOnNext(Integer value) {
				SIZE++;
				log.info("# onNext: " + value);
				if (SIZE == 2) {
					request(2);
					SIZE = 0;
				}
			}

			@Override
			protected void hookOnComplete() {
				log.info("# onComplete");
			}
		});
	}

	private static void ex10_create() {
		Flux.create((FluxSink<Integer> sink) -> {
				// push 방식으로 데이터를 emit
				for (int i = 0; i < 10; i++) {
					sink.next(i);
				}

				sink.complete();
			})
			.subscribe(
				data -> log.info("# onNext: " + data),
				error -> {
				},
				() -> log.info("# onComplete"));
	}

	private static int start = 1;
	private static int end = 4;

	@SneakyThrows
	private static void ex11_create() {
		Flux.create((FluxSink<Integer> sink) -> {
				sink.onRequest(n -> {
					log.info("# onRequest: {}", n);
					try {
						Thread.sleep(500L);
						// 4개씩 데이터를 emit
						for (int i = start; i <= end; i++) {
							sink.next(i);
						}
						start += 4;
						end += 4;
					} catch (Exception e) {
					}
				});

				sink.onDispose(() -> log.info("# clean up"));
			}, FluxSink.OverflowStrategy.DROP) // request 갯수를 초과한 데이터는 drop
			.subscribeOn(Schedulers.boundedElastic())
			.publishOn(Schedulers.parallel(), 2) // 2개씩 request
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(3000L);
	}

	/// 2. 필터링 Operator
	private static void ex12_filter() {
		Flux.range(1, 10)
			.filter(i -> i % 2 == 0)
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	@SneakyThrows
	private static void ex13_filterWhen() {
		Flux.range(1, 10)
			.filterWhen(i -> Mono.just(i <= 5).publishOn(Schedulers.parallel())) // 비동기적으로 조건을 평가
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(1000);
	}

	@SneakyThrows
	private static void ex14_skip() {
		Flux.interval(Duration.ofSeconds(1))
			.skip(2) // 처음 2개의 데이터를 skip
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(5500);
	}

	@SneakyThrows
	private static void ex15_skipTime() {
		Flux.interval(Duration.ofSeconds(1))
			.skip(Duration.ofSeconds(3)) // 처음 3초 동안의 데이터를 skip
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(5000);
	}

	@SneakyThrows
	private static void ex16_take() {
		Flux.interval(Duration.ofMillis(500))
			.take(3) // 처움 3개의 데이터 emit 후 cancel signal 전송
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(3000);
	}

	@SneakyThrows
	private static void ex17_takeTime() {
		Flux.interval(Duration.ofMillis(500))
			.take(Duration.ofSeconds(2)) // 2초 동안의 데이터 emit 후 cancel signal 전송
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(3000);
	}

	private static void ex18_takeLast() {
		Flux.range(1, 10)
			.takeLast(3) // 마지막 3개의 데이터만 emit
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	private static void ex19_takeUntil() {
		Flux.range(1, 10)
			.takeUntil(i -> i > 5) // 5보다 큰 데이터가 나오기 전까지 emit (조건에 맞는 데이터 포함)
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	private static void ex20_takeWhile() {
		Flux.range(1, 10)
			.takeWhile(i -> i < 5) // 5보다 작은 데이터만 emit (조건에 맞지않는 데이터는 포함하지 않음)
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	private static void ex21_next() {
		Flux.range(1, 10)
			.next() // Flux에서 단일 요소를 Mono로 변환
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	/// 3. 변환 Operator
	private static void ex22_map() {
		Flux.range(1, 5)
			.map(i -> i * 2) // 각 요소를 2배로 변환 (동기)
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	@SneakyThrows
	private static void ex23_flatMap() {
		Flux.just(2, 3, 4)
			.flatMap(dan -> Flux.range(1, 9) // inner sequence들을 flatten하고, 하나의 sequence로 merge (비동기)
				.publishOn(Schedulers.parallel())
				.map(i -> dan + " * " + i + " = " + (dan * i)))
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(2000);
	}

	private static void ex24_concat() {
		Flux.concat(
				Flux.just("A", "B", "C"),
				Flux.just("D", "E", "F")) // 두 Flux를 순차적으로 연결
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	@SneakyThrows
	private static void ex25_merge() {
		Flux.merge(
				Flux.just("A", "B", "C").delayElements(Duration.ofMillis(50)),
				Flux.just("D", "E", "F").delayElements(Duration.ofMillis(30))) // 두 Flux를 인터리빙 방식으로 병합(emit 된 시간 순서대로)
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(1000);
	}

	@SneakyThrows
	private static void ex26_zip1() {
		Flux.zip(
				Flux.just("A", "B", "C").delayElements(Duration.ofMillis(50)),
				Flux.just(1, 2, 3).delayElements(Duration.ofMillis(30)),
				Flux.just("a", "b", "c").delayElements(Duration.ofMillis(100))) // 두개 이상의 데이터를 Tuple로 결합하여 emit. 결합에 필요한 데이터를 기다림
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(1000);
	}

	@SneakyThrows
	private static void ex27_zip2() {
		Flux.zip(
				Flux.just("A", "B", "C").delayElements(Duration.ofMillis(50)),
				Flux.just(1, 2, 3).delayElements(Duration.ofMillis(30)),
				(x, y) -> x + " " + y) // combinator로 두 데이터 하나의 데이터로 결합
			.subscribe(data -> log.info("# onNext: {}", data));

		Thread.sleep(1000);
	}

	@SneakyThrows
	private static void ex28_and() {
		// 모든 작업이 끝난 시점에, 최종적으로 후처리 작업을 수행할때 사용
		Mono.just("Task 1")
			.delayElement(Duration.ofSeconds(1))
			.doOnNext(data -> log.info("# Mono doOnNext: {}", data))
			.and(Flux.just("Task 2", "Task 3")
				.delayElements(Duration.ofMillis(600))
				.doOnNext(data -> log.info("# Flux doOnNext: {}", data))) // Mono의 Complete Signal과 파라미터 publisher의 Complete Signal이 모두 발생하면 Complete Signal 발생 (Mono<Void> 반환)
			.subscribe(
				data -> log.info("# onNext: {}", data), // 데이터는 전달되지 않기 때문에 호출되지 않음
				error -> {
				},
				() -> log.info("# onComplete"));

		Thread.sleep(5000);
	}

	private static void ex29_collectList() {
		// Flux의 모든 요소를 List로 변환 후, 이 List를 emit하는 Mono를 반환
		Flux.range(1, 5)
			.collectList() // 모든 요소를 List로 수집
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	private static void ex30_collectMap() {
		// Flux의 모든 요소를 Map으로 변환 후, 이 Map을 emit하는 Mono를 반환
		Flux.range(1, 5)
			.collectMap(
				i -> "Key" + i,
				i -> "Value" + i) // 각 요소를 Key-Value 쌍으로 수집
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	/// 4. 내부 동작 확인을 위한 Operator
	private static void ex31_doOnXXX() {
		// emit되는 데이터의 변경 없이 부수효과만을 수행하기 위한 연산자들
		Flux.range(1, 5)
			.doFirst(() -> log.info("# doFirst")) // 구독이 시작되기 전에 실행
			.doOnSubscribe(subscription -> log.info("# doOnSubscribe: {}", subscription)) // 구독이 시작될 때 실행
			.doOnRequest(n -> log.info("# doOnRequest: {}", n))
			.doOnNext(data -> log.info("# doOnNext: {}", data)) // emit 발생 시
			.doOnEach(data -> log.info("# doOnEach: {}", data)) // emit, complete, error 발생 시
			.doOnDiscard(Integer.class, data -> log.info("# doOnDiscard: {}", data)) // 필터링된 데이터가 discard될 때 실행
			.doOnComplete(() -> log.info("# doOnComplete"))
			.doOnError(error -> log.error("# doOnError: {}", error.getMessage()))
			.doOnCancel(() -> log.info("# doOnCancel"))
			.doOnTerminate(() -> log.info("# doOnTerminate")) // complete, error 발생 시
			.doFinally(signalType -> log.info("# doFinally: {}", signalType)) // complete, error, cancel 등 모든 종료 시점에 실행
			.doAfterTerminate(() -> log.info("# doAfterTerminate")) // Publisher가 종료된 후 실행
			.subscribe();
	}

	/// 5. 에러 처리를 위한 Operator
	private static void ex32_error() {
		Flux.range(1, 5)
			.flatMap(i -> {
				if ((i * 2) % 3 == 0) {
					return Flux.error(new IllegalArgumentException("Error at " + i));
				} else {
					return Mono.just(i * 2);
				}
			})
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: {}", error.getMessage()));
	}

	private static Mono<String> convert(char letter) throws DataFormatException {
		if (!Character.isAlphabetic(letter)) {
			throw new DataFormatException("Not Alphabetic : " + letter);
		}
		return Mono.just("Converted to " + Character.toUpperCase(letter));
	}

	private static void ex33_error() {
		Flux.just('a', 'b', 'c', '3', 'e')
			.flatMap(letter -> {
				try {
					return convert(letter);
				} catch (DataFormatException e) {
					return Flux.error(e);
				}
			})
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	private static void ex34_onErrorReturn() {
		Flux.just('a', 'b', 'c', '3', 'e')
			.flatMap(letter -> {
				try {
					return convert(letter);
				} catch (DataFormatException e) {
					return Flux.error(e);
				}
			})
			.onErrorReturn("Error occurred, returning default value") // 에러 발생 시 기본값 반환
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	private static void ex35_onErrorReturn() {
		Flux.just('a', 'b', 'c', '3', 'e')
			.flatMap(letter -> {
				try {
					return convert(letter);
				} catch (DataFormatException e) {
					return Flux.error(e);
				}
			})
			.onErrorReturn(NullPointerException.class, "NullPointerException")
			.onErrorReturn(DataFormatException.class, "Error occurred, returning default value") // 특정 예외 발생 시 기본값 반환
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	private static void ex36_onErrorResume() {
		Flux.just('a', 'b', 'c', '3', 'e')
			.flatMap(letter -> {
				try {
					return convert(letter);
				} catch (DataFormatException e) {
					return Flux.error(e);
				}
			})
			.onErrorResume( error -> Mono.just("default value")) // 에러 발생 시 동적으로 Publisher를 반환
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	private static void ex37_onErrorContinue() {
		// 의도하지 않은 상황을 발생시킬 수 있기 떄문에 지양
		// 대부분의 에러는 doOnError()로 로깅 및 onErrorResume(), onErrorReturn() 등을 사용하여 처리
		Flux.just('a', 'b', 'c', '3', 'e')
			.flatMap(letter -> {
				try {
					return convert(letter);
				} catch (DataFormatException e) {
					return Flux.error(e);
				}
			})
			.onErrorContinue((error, number) -> log.error("# onErrorContinue: {}, {}", error.getMessage(), number)) // 에러가 발생해도, 후속 데이터를 계속 emit
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	@SneakyThrows
	private static void ex38_retry() {
		// lambda 내부에서는 final 변수나 effectively final 변수를 사용할 수 있지만, 람다 외부에서 선언된 변수는 사용할 수 없다.
		AtomicInteger count = new AtomicInteger(1);
		// final int[] count = {1}; // 혹은 final 배열로 선언.

		Flux.range(1, 3)
			.delayElements(Duration.ofSeconds(1))
			.map(num -> {
				try {
					if (num == 3 && count.get() == 1) {
						count.incrementAndGet();
						Thread.sleep(1000);
					}
				} catch (Exception e) {}

				return num;
			})
			.timeout(Duration.ofMillis(1500))
			.retry(1)
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: {}", error.getMessage()),
				() -> log.info("# onComplete")
			);

		Thread.sleep(7000);
	}

	/// 6. 동작 시간 측정을 위한 Operator
	@SneakyThrows
	private static void ex39_elapsed() {
		// Flux의 각 요소가 emit되는 시간을 측정
		Flux.range(1, 5)
			.delayElements(Duration.ofMillis(500))
			.elapsed() // emit된 데이터 사이의 시간 간격을 측정해서 Tuple로 결합 하여 emit
			.subscribe(data -> log.info("# onNext: {}, elapsed time: {} ms", data.getT2(), data.getT1()));

		Thread.sleep(6000);
	}

	/// 7. Sequence 분할을 위한 Operator
	private static void ex40_window() {
		Flux.range(1, 11)
			.window(3)// 3개씩 데이터를 분할하여 Flux<Flux<Integer>> 형태로 반환
			.flatMap(flux -> {
				log.info("========== Window Start ==========");
				return flux;
			})
			.subscribe(buffer -> log.info("# onNext: {}", buffer));
	}

	private static void ex41_buffer() {
		// 오류로 인해 buffer가 채워지지 않는 경우, 무한대기에 빠질 수 있음. bufferTimeout 사용을 지향
		Flux.range(1, 95)
			.buffer(10) // 10개씩 데이터를 모아서 List로 반환
			.subscribe(buffer -> log.info("# onNext: {}", buffer));
	}

	private static void ex42_bufferTimeout() {
		Flux.range(1, 20)
			.map(num -> {
				try {
					if (num < 10) {
						Thread.sleep(100); // 10개까지는 빠르게 emit
					} else {
						Thread.sleep(500); // 10개 이후부터는 느리게 emit
					}
				} catch (Exception e) {}
				return num;
			})
			.bufferTimeout(3, Duration.ofMillis(400)) // 갯수 또는 시간 조건 중 먼저 만족하는 조건으로 버퍼링
			.subscribe(buffer -> log.info("# onNext: {}", buffer));
	}

	private static void ex43_groupBy() {
		Flux.range(1, 30)
			.groupBy(i -> i % 3) // 3으로 나눈 나머지로 그룹화
			.flatMap(groupedFlux -> groupedFlux.map(i -> "Group " + groupedFlux.key() + ": " + i))
			.subscribe(data -> log.info("# onNext: {}", data));
	}

	/// 8. Multicasting을 위한 Operator
	@SneakyThrows
	private static void ex44_publish() {
		// 구독 시점에 데이터를 emit하지 않고, connect() 호출 시점에 데이터를 emit
		ConnectableFlux<Integer> flux = Flux.range(1, 5)
			.delayElements(Duration.ofMillis(300))
			.publish();

		Thread.sleep(500);
		flux.subscribe(data -> log.info("# Subscriber 1 onNext: {}", data));

		Thread.sleep(200);
		flux.subscribe(data -> log.info("# Subscriber 2 onNext: {}", data));

		flux.connect();

		Thread.sleep(1000);
		flux.subscribe(data -> log.info("# Subscriber 3 onNext: {}", data));

		Thread.sleep(2000);
	}

	@SneakyThrows
	private static void ex45_autoConnect() {
		// connect()를 명시적으로 호출하지 않고 자동으로 연결
		Flux<Integer> flux = Flux.range(1, 5)
			.delayElements(Duration.ofMillis(300))
			.publish()
			.autoConnect(2); // 2개의 구독자가 있을 때 자동으로 연결

		Thread.sleep(500);
		flux.subscribe(data -> log.info("# Subscriber 1 onNext: {}", data));

		Thread.sleep(200);
		flux.subscribe(data -> log.info("# Subscriber 2 onNext: {}", data));

		Thread.sleep(1000);
		flux.subscribe(data -> log.info("# Subscriber 3 onNext: {}", data));

		Thread.sleep(2000);
	}

	@SneakyThrows
	private static void ex46_refCount() {
		// 파라미터로 입력한 수 만큼 구독이 발생하면 데이터 emit을 시작하고, 구독이 모두 종료되면 자동으로 연결 해제
		// 주로 무한스트림에서 모든 구독이 취소될 경우, 연결 해제하는데 사용
		Flux<Long> publisher = Flux.interval(Duration.ofMillis(500))
			// .publish().autoConnect(2)
			.publish().refCount(1);
		Disposable disposable = publisher.subscribe(data -> log.info("# Subscriber 1 onNext: {}", data));

		Thread.sleep(2100);
		disposable.dispose(); // 모든 구독이 종료되어 publisher의 emit이 중지됨

		publisher.subscribe(data -> log.info("# Subscriber 2 onNext: {}", data)); // 다시 구독이 발생하면, publisher가 다시 데이터를 emit하기 시작함
		Thread.sleep(2500);
	}

	public static void main(String[] args) {
		/// 1. 생성 Operator
		// ex1_justOrEmpty();
		// ex2_fromXXX();
		// ex3_range();
		// ex4_defer1();
		// ex5_defer2();
		// ex6_using();
		// ex7_generate1();
		// ex8_generate2();
		// ex9_create();
		// ex10_create();
		// ex11_create();

		///  2. 필터링 Operator
		// ex12_filter();
		// ex13_filterWhen();
		// ex14_skip();
		// ex15_skipTime();
		// ex16_take();
		// ex17_takeTime();
		// ex18_takeLast();
		// ex19_takeUntil();
		// ex20_takeWhile();
		// ex21_next();

		/// 3. 변환 Operator
		// ex22_map();
		// ex23_flatMap();
		// ex24_concat();
		// ex25_merge();
		// ex26_zip1();
		// ex27_zip2();
		// ex28_and();
		// ex29_collectList();
		// ex30_collectMap();

		/// 4. 내부 동작 확인을 위한 Operator
		// ex31_doOnXXX();

		/// 5. 에러 처리를 위한 Operator
		// ex32_error();
		// ex33_error();
		// ex34_onErrorReturn();
		// ex35_onErrorReturn();
		// ex36_onErrorResume();
		// ex37_onErrorContinue();
		// ex38_retry();

		///  6. 동작 시간 측정을 위한 Operator
		// ex39_elapsed();

		/// 7. Sequence 분할을 위한 Operator
		// ex40_window();
		// ex41_buffer();
		// ex42_bufferTimeout();
		// ex43_groupBy();

		/// 8. Multicasting을 위한 Operator
		// ex44_publish();
		// ex45_autoConnect();
		ex46_refCount();
	}
}
