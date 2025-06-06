package toy.lsd.playground.p2temperature;

import java.util.List;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Observable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TemperatureController {
    @Autowired
    private TemperatureSensor temperatureSensor;

    // 방법 1: RxJava Observable을 직접 반환 (Spring WebFlux가 자동으로 처리)
    @GetMapping(value = "/temperature/stream-observable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Observable<Temperature> getTemperatureStreamObservable() {
        return temperatureSensor.temperatureStream().take(10); // 10개의 값만 반환
    }

    // 방법 2: Observable을 Flux로 변환하여 반환
    @GetMapping(value = "/temperature/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Temperature> getTemperatureStreamFlux() {
        Observable<Temperature> observable = temperatureSensor.temperatureStream().take(10);
        return RxJava3Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER);
    }

    // 방법 3: 단일 값을 Mono로 반환
    @GetMapping("/temperature/current")
    public Mono<Temperature> getCurrentTemperature() {
        Observable<Temperature> observable = temperatureSensor.temperatureStream().take(1);
        return RxJava3Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER).next();
    }

    // 방법 4: Server-Sent Events (SSE)로 스트리밍
    @GetMapping(value = "/temperature/stream-sse", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Temperature> getTemperatureStreamSSE() {
        Observable<Temperature> observable = temperatureSensor.temperatureStream().take(20);
        return RxJava3Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER);
    }

    // 방법 5: 여러 값을 리스트로 수집하여 반환
    @GetMapping("/temperature/batch")
    public Mono<List<Temperature>> getTemperatureBatch() {
        Observable<Temperature> observable = temperatureSensor.temperatureStream().take(5);
        return RxJava3Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
                .collectList();
    }
}
