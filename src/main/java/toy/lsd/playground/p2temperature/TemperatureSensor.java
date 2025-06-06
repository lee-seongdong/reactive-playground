package toy.lsd.playground.p2temperature;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Observable;

@Component
public class TemperatureSensor {
	private final Random random = new Random();

	private final Observable<Temperature> dataStream = Observable.range(0, Integer.MAX_VALUE)
		.concatMap(tick -> Observable.just(tick)
			.delay(random.nextInt(5000), TimeUnit.MILLISECONDS)
			.map(tickValue -> this.probe())
		);

	private Temperature probe() {
		return new Temperature(16 + random.nextGaussian() * 10);
	}

	public Observable<Temperature> temperatureStream() {
		return dataStream;
	}
}
