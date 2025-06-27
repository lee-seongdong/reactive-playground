package toy.lsd.reactor;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.time.Duration;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

/**
 * StepVerifier: Reactor Sequence에서 발생한 Signal 이벤트를 테스트
 */
public class C8TestingTest {
	private static final Logger log = LoggerFactory.getLogger(C8TestingTest.class);

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
	public void contextTest() {
		Mono<String> source = Mono.just("hello");

		StepVerifier
			.create(C8Testing.getSecretMessage(source)
				.contextWrite(context -> context.put("secretMessage", "Hello Reactor"))
				.contextWrite(context -> context.put("secretKey", "aGVsbG8=")))
			.expectSubscription().as("# expect subscription")
			.expectAccessibleContext() // 기댓값 : 구독 이후 Context가 전파되었음
			.hasKey("secretMessage") // 기댓값 : 전파된 Context에 키에 해당하는 값이 있음
			.hasKey("secretKey")
			.then() // Sequence의 다음 Signal 기댓값 평가
			.expectNext("Hello Reactor") // 기댓값 : Hello, Reactor가 emit됨
			.expectComplete() // 기댓값 : onComplete Signal을 전달받음
			.verify();
	}

	@Test
	public void recordTest1() {
		StepVerifier
			.create(C8Testing.getCapitalizedCountry(Flux.just("korea", "england", "canada", "india")))
			.expectSubscription().as("# expect subscription")
			.recordWith(ArrayList::new)	// emit된 데이터 기록할 컬렉션 생성
			.thenConsumeWhile(country -> {
				log.info("# thenConsume: {}", country);
				return !country.isEmpty();
			}) // 조건에 맞는 데이터는 다음 단계에서 소모
			.consumeRecordedWith(countries -> {
				log.info("# consumeRecorded: {}", countries);
				assertThat(countries.stream().allMatch(country -> Character.isUpperCase(country.charAt(0))), is(true));
			}) // 기록된 데이터 소모
			.expectNextCount(0) // emit된 데이터를 기록하고 소모했으므로 기댓값 : 0
			.expectComplete()
			.verify();
	}

	@Test
	public void recordTest2() {
		StepVerifier
			.create(C8Testing.getCapitalizedCountry(Flux.just("korea", "england", "canada", "india")))
			.expectSubscription().as("# expect subscription")
			.recordWith(ArrayList::new)
			.thenConsumeWhile(country -> !country.isEmpty())
			.expectRecordedMatches(countries ->
				countries.stream().allMatch(country -> Character.isUpperCase(country.charAt(0))))
			.expectNextCount(0)
			.expectComplete()
			.verify();
	}

	/**
	 * 정상 동작하는 TestPublisher
	 * - emit 데이터가 null이 아닌지 체크
	 * - 요청하는 갯수보다 더 많은 데이터를 emit 하는지 체크
	 */
	@Test
	public void testPublisherTest1() {
		TestPublisher<Integer> source = TestPublisher.create(); // 프로그래밍 방식으로 데이터를 emit

		StepVerifier
			.create(C8Testing.divideByTwo(source.flux()))
			.expectSubscription().as("# expect subscription")
			.then(() -> source.emit(2, 4, 6, 8, 10)) // emit 지점
			.expectNext(1, 2, 3, 4)
			.expectError()
			.verify();
	}

	/**
	 * 오동작하는 TestPublisher
	 * 리액티브 스트림즈 사양을 위반하더라도 데이터를 emit할 수 있음
	 */
	@Test
	public void testPublisherTest2() {
		/*
		- ALLOW_NULL: null 데이터 emit 허용
		- CLEANUP_ON_TERMINATE: onComplete, onError등 Terminal Signal을 여러번 보내는 것을 허용
		- DEFER_CANCELLATION: cancel Signal을 무시하고 계속해서 Signal을 emit 하는 것을 허용
		- REQUEST_OVERFLOW: 요청 갯수보다 많은 Signal이 발생하는 것을 허용
		 */
		TestPublisher<Integer> source = TestPublisher.createNoncompliant(TestPublisher.Violation.ALLOW_NULL); // null emit 허용

		StepVerifier
			.create(C8Testing.divideByTwo(source.flux()))
			.expectSubscription().as("# expect subscription")
			.then(() -> source.emit(2, 4, null, 8, 10)) // emit 지점
			.expectNext(1, 2, 3, 4)
			.expectError()
			.verify();
	}

	@Test
	public void testPublisherProbe() {
		// PublisherProbe: Publisher를 테스트하기 위한 모듈. switch 등 경로가 분기되는 상황에 주로 쓰임
		// Spy와 유사함
		PublisherProbe<String> probe = PublisherProbe.of(C8Testing.supplyStandbyPower());

		StepVerifier
			.create(C8Testing.processTask(
				C8Testing.supplyMainPower(),
				probe.mono()))
			.expectNextCount(1) // mainPower를 대신해 standbyPower로부터 데이터를 받음
			.verifyComplete();

		probe.assertWasSubscribed(); // 기댓값 : standbyPower가 subscribe됨
		probe.assertWasRequested(); // 기댓값 : standbyPower가 데이터 요청을 받음
		probe.assertWasNotCancelled(); // 기댓값 : standbyPower가 중간에 취소되지 않음
	}
}
