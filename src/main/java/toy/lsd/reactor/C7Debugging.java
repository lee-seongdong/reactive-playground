package toy.lsd.reactor;

import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

/**
 * Assembly : Operator가 리턴하는 Mono / Flux가 선언된 지점
 * debugMode : Operator의 Assembly 정보를 캡처하기 위한 모드
 * Traceback : 에러가 발생한 Operator의 스택트레이스를 캡처한 Assembly정보. Suppressed Exception 형태로 원본 스택트레이스에 추가됨.
 */
@Slf4j
public class C7Debugging {
	private static final Map<String, String> fruits = Map.of(
		"banana", "바나나",
		"apple", "사과",
		"pear", "배",
		"grape", "포도");

	@SneakyThrows
	private static void ex1_debugMode() {
		// 디버그로 코드를 실행하면 자동으로 활성화 (intellij 설정)
		Hooks.onOperatorDebug(); // 모든 Operator에서 스택트레이스를 캡쳐 (성능 하락)

		Flux.just("BANANAS", "APPLES", "PEARS", "MELONS")
			.subscribeOn(Schedulers.boundedElastic())
			.publishOn(Schedulers.parallel())
			.map(String::toLowerCase)
			.map(fruit -> fruit.substring(0, fruit.length() - 1))
			.map(fruits::get)
			.map(translated -> "맛있는 " + translated)
			.subscribe(log::info,
				error -> log.error("# onError: ", error));

		Thread.sleep(100L);
	}

	@SneakyThrows
	private static void ex2_checkpoint1() {
		Flux.just(2, 4, 6, 8)
			.zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x / y)
			.checkpoint("zipwith", true)
			.map(num -> num + 2)
			.checkpoint() // 발생한 에러가 전파되면, 호출한 지점의 traceback을 추가함 (up -> down으로 전파)
			.subscribe(
				data -> log.info("# onNext: {}", data),
				error -> log.error("# onError: ", error)
			);
	}

	private static Flux<Integer> multiply(Flux<Integer> source, Flux<Integer> other) {
		return source.zipWith(other, (x, y) -> x / y);
	}

	private static Flux<Integer> plus(Flux<Integer> source) {
		return source.map(x -> x + 2);
	}

	private static void ex3_checkpoint2() {
		// 에러 발생지점이 여러곳인 경우, Source부터 checkpoint 추가 후 범위를 좁혀가면서 찾을 수 있다.
		Flux<Integer> source = Flux.just(2, 4, 6, 8).checkpoint();
		Flux<Integer> other = Flux.just(1, 2, 3, 0).checkpoint();

		Flux<Integer> multiplySource = multiply(source, other);
		Flux<Integer> plusSource = plus(multiplySource);

		plusSource.subscribe(
			data -> log.info("# onNext: {}", data),
			error -> log.error("# onError: ", error));
	}

	private static void ex4_log() {
		Flux.just("BANANAS", "APPLES", "PEARS", "MELONS")
			.map(String::toLowerCase)
			.map(fruit -> fruit.substring(0, fruit.length() - 1)).log("TEST**") // 해당 지점에서 발생하는 signal을 로깅
			.map(fruits::get)
			.subscribe(log::info,
				error -> log.error("# onError: ", error));
	}

	public static void main(String[] args) {
		// ex1_debugMode();
		// ex2_checkpoint1();
		// ex3_checkpoint2();
		ex4_log();
	}
}
