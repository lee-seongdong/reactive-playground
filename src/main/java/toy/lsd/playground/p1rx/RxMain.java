package toy.lsd.playground.p1rx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class RxMain {
	// #1 기본 observe, subscribe
	private static void practice1() {
		Observable.create(emitter -> {
			emitter.onNext("Hello1");
			emitter.onNext("Hello2");
			emitter.onNext("Hello3");
			emitter.onError(new RuntimeException("Error"));
			emitter.onNext("Hello4");
			emitter.onNext("Hello5");
			emitter.onComplete();
		}).subscribe(
			System.out::println,
			System.err::println,
			() -> System.out.println("Done")
		);

		// Observable<String> hello = Observable.fromCallable(() -> "Hello ");
		// Future<String> future = Executors.newCachedThreadPool().submit(() -> "word");
		// Observable<String> world = Observable.fromFuture(future);
		// Observable.concat(hello, world, Observable.just("!!")).forEach(System.out::println);
	}

	// #2 비동기 시퀀스
	private static void practice2() throws InterruptedException {
		Observable.interval(1, TimeUnit.SECONDS)
			.subscribe(e -> System.out.println("Receive: " + e));
		Thread.sleep(5000);
	}

	// #3 비동기 시퀀스 + 구독 취소
	private static void practice3() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(5);

		Disposable d = Observable.interval(1, TimeUnit.SECONDS)
			.subscribe(e -> {
				System.out.println("Receive await: " + e);
				latch.countDown();
			});

		latch.await();
		d.dispose();
	}

	// #4 stream zip
	private static void practice4() {
		Observable.zip(
			Observable.just("A", "B", "C"),
			Observable.just("1", "2", "3"),
			(a, b) -> a + b
		).forEach(System.out::println);
	}

	public static void main(String[] args) throws Exception {
		practice4();
	}
}
