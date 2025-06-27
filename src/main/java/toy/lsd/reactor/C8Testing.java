package toy.lsd.reactor;

import java.util.Base64;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

// test.java.toy.lsd.reactor.C8Testing
public class C8Testing {
	public static Flux<String> sayHello() {
		return Flux.just("Hello", "Reactor");
	}

	public static Flux<Integer> divideByTwo(Flux<Integer> source) {
		return source.zipWith(Flux.just(2, 2, 2, 2, 0), (x, y) -> x / y);
	}

	public static Flux<Integer> takeNumber(Flux<Integer> source, long n) {
		return source.take(n);
	}

	public static Flux<Tuple2<String, Integer>> getCOVID19Count(Flux<Long> source) {
		return source
			.flatMap(notUse -> Flux.just(
				Tuples.of("서울", 10),
				Tuples.of("경기도", 5),
				Tuples.of("강원도", 3),
				Tuples.of("충청도", 6),
				Tuples.of("경상도", 5),
				Tuples.of("전라도", 8),
				Tuples.of("인천", 2),
				Tuples.of("대구", 1),
				Tuples.of("대전", 2),
				Tuples.of("부산", 3),
				Tuples.of("제주도", 0)
			));
	}

	public static Flux<Tuple2<String, Integer>> getVoteCount(Flux<Long> source) {
		return source
			.zipWith(Flux.just(
				Tuples.of("중구", 15400),
				Tuples.of("서초구", 20020),
				Tuples.of("강서구", 32040),
				Tuples.of("강동구", 14506),
				Tuples.of("서대문구", 35650)
			))
			.map(Tuple2::getT2);
	}

	// Backpressure 테스트를 위한 메소드
	public static Flux<Integer> generateNumber() {
		return Flux.create(emitter -> {
			for (int i = 0; i <= 100; i++) {
				emitter.next(i);
			}
			emitter.complete();
		}, FluxSink.OverflowStrategy.ERROR);
	}

	public static Mono<String> getSecretMessage(Mono<String> keySource) {
		return keySource
			.zipWith(Mono.deferContextual(contextView -> Mono.just((String)contextView.get("secretKey"))))
			.filter(tp -> tp.getT1().equals(new String(Base64.getDecoder().decode(tp.getT2()))))
			.transformDeferredContextual(((mono, contextView) -> mono.map(notUse -> contextView.get("secretMessage"))));
	}
}
