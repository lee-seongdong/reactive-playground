package toy.lsd.reactor;

import java.time.Duration;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.test.scheduler.VirtualTimeScheduler;

public class C8TestingTest {
	/**
	 * Reactor Sequence에서 발생한 Signal 이벤트를 테스트
	 */
	@Test
	public void stepVerifier1() {
		StepVerifier
			.create(Mono.just("Hello world")) // 테스트 대상 데이터 생성
			.expectNext("Hello world") // emit 데이터 기댓값 평가
			.expectComplete() // complete signal 평가
			.verify(); // 검증 실행
	}

	@Test
	public void stepVerifier2() {
		StepVerifier
			.create(C8Testing.sayHello())
			.expectSubscription().as("# expect subscription") // as로 이전 기댓값 평가에 대한 설명 추가
			.expectNext("Hi").as("# expect Hi")
			.expectNext("Reactor").as("# expect Reactor")
			.verifyComplete();
	}

	@Test
	public void stepVerifier3() {
		Flux<Integer> source = Flux.just(2, 4, 6, 8, 10);
		StepVerifier
			.create(C8Testing.divideByTwo(source))
			.expectSubscription().as("# expect subscription")
			.expectNext(1)
			.expectNext(2)
			.expectNext(3)
			.expectNext(4)
			// .expectNext(1, 2, 3, 4)
			.expectError()
			.verify();
	}

	@Test
	public void stepVerifier4() {
		Flux<Integer> source = Flux.range(0, 1000);
		StepVerifier
			.create(C8Testing.takeNumber(source, 500),
				StepVerifierOptions.create().scenarioName("Verify from 0 to 499"))
			.expectSubscription().as("# expect subscription")
			.expectNext(0).as("# expect 0")
			.expectNextCount(498)
			.expectNext(499)
			// .expectNext(500)
			.expectComplete()
			.verify();
	}

	@Test
	public void timeBasedStepVerifier1() {
		StepVerifier
			.withVirtualTime(() -> C8Testing.getCOVID19Count(Flux.interval(Duration.ofHours(1)).take(1))) // 가상 시간 정보로부터 1시간 후 COVID정보 조회
			.expectSubscription().as("# expect subscription")
			.then(() -> VirtualTimeScheduler
				.get()
				.advanceTimeBy(Duration.ofHours(1))) // 가상 시간정보를 1시간 당김
			.expectNextCount(11)
			.expectComplete()
			.verify();
	}

	@Test
	public void timeBasedStepVerifier2() {
		StepVerifier
			.create(C8Testing.getCOVID19Count(Flux.interval(Duration.ofMinutes(1)).take(1)))
			.expectSubscription().as("# expect subscription")
			.expectNextCount(11)
			.expectComplete()
			.verify(Duration.ofSeconds(3)); // 타임아웃 테스트
	}

	@Test
	public void timeBasedStepVerifier3() {
		StepVerifier
			.withVirtualTime(() -> C8Testing.getVoteCount(Flux.interval(Duration.ofMinutes(1))))
			.expectSubscription().as("# expect subscription")
			.expectNoEvent(Duration.ofMinutes(1)) // 아무런 이벤트가 없을것이라는 기대와 동시에 지정한 시간만큼 가상 시간정보를 당김
			.expectNoEvent(Duration.ofMinutes(1))
			.expectNoEvent(Duration.ofMinutes(1))
			.expectNoEvent(Duration.ofMinutes(1))
			.expectNoEvent(Duration.ofMinutes(1))
			.expectNextCount(5)
			.expectComplete()
			.verify();
	}

	@Test
	public void backpressureTest() {
		StepVerifier
			.create(C8Testing.generateNumber(), 1L) // initialRequest(1)
			.thenConsumeWhile(number -> number >= 1)
			.verifyComplete(); // fail
	}

	@Test
	public void backpressureTest2() {
		StepVerifier
			.create(C8Testing.generateNumber(), 1L) // overflow 에러 발생
			.thenConsumeWhile(number -> number >= 1)
			.expectError() // 기댓값 : 에러발생
			.verifyThenAssertThat() // 검증 트리거 후 추가적인 assert
			.hasDroppedElements(); // 기댓값 : drop된 데이터가 있음
	}

	@Test
	public void contextTest1() {
		// ??
		Mono<String> source = Mono.just("hello");

		StepVerifier
			.create(
				C8Testing.getSecretMessage(source)
					.contextWrite(context -> context.put("secretMessage", "Hello Reactor"))
					.contextWrite(context -> context.put("secretKey", "aGVsbG8=")))
			.expectSubscription().as("# expect subscription")
			.expectAccessibleContext() // 기댓값 : 구독 이후 Context가 전파되었음
			.hasKey("secretMessage") // 기댓값 : 전파된 Context에 키에 해당하는 값이 있음
			.hasKey("secretKey")
			.then() // Sequence의 다음 Signal 기댓값 평가
			.expectNext("Hello, Reactor") // 기댓값 : Hello Reactor가 emit됨
			.expectComplete() // 기댓값 : onComplete Signal을 전달받음
			.verify();

	}
}
