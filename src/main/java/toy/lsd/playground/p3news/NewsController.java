package toy.lsd.playground.p3news;

import io.reactivex.rxjava3.processors.PublishProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import io.reactivex.rxjava3.core.BackpressureStrategy;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsPublisher newsPublisher;
    
    @Autowired
    private NewsSubscriber newsSubscriber;

    // 구독 기능
    @PostMapping("/subscribe")
    public String subscribe() {
        newsSubscriber.subscribe(newsPublisher.getNewsStream());
        return "Subscribed successfully. Subscriber ID: " + newsSubscriber.getSubscriberId();
    }

    // 뉴스 요청 기능
    @PostMapping("/request/{count}")
    public String requestNews(@PathVariable int count) {
        int actualRequest = newsSubscriber.requestNews(count);
        newsPublisher.publishNews(actualRequest);
        return "Requested " + actualRequest + " news items (originally requested: " + count + ")";
    }

    // 구독 취소 기능
    @PostMapping("/unsubscribe")
    public String unsubscribe() {
        newsSubscriber.unsubscribe();
        return "Unsubscribed successfully. Subscriber ID: " + newsSubscriber.getSubscriberId();
    }

    // 사서함에서 뉴스 읽기
    @GetMapping("/read")
    public List<News> readNews() {
        List<News> newsList = new ArrayList<>();
        News news;
        while ((news = newsSubscriber.readNews()) != null) {
            newsList.add(news);
        }
        return newsList;
    }

    // 사서함 상태 확인
    @GetMapping("/mailbox/status")
    public String getMailboxStatus() {
        return String.format("Mailbox status - Used: %d/5, Available: %d", 
                newsSubscriber.getMailboxSize(), 
                newsSubscriber.getAvailableSpace());
    }

    // 🆕 Processor의 장점 1: Reactive Streams 표준 호환성
    @GetMapping(value = "/stream/processor", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<News> streamViaProcessor() {
        PublishProcessor<News> processor = newsPublisher.getNewsProcessor();
        // Processor는 Flowable의 하위 클래스이므로 직접 변환 가능
        return RxJava3Adapter.flowableToFlux(processor);
    }

    // 🆕 Processor의 장점 2: Server-Sent Events with Backpressure
    @GetMapping(value = "/stream/sse-processor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<News> streamSSEViaProcessor() {
        PublishProcessor<News> processor = newsPublisher.getNewsProcessor();
        // 백프레셔 전략을 적용한 Flowable로 변환
        return RxJava3Adapter.flowableToFlux(
            processor.onBackpressureDrop() // 백프레셔 전략: DROP
        );
    }

    // 🆕 테스트용 뉴스 발행
    @PostMapping("/publish/test/{count}")
    public String publishTestNews(@PathVariable int count) {
        newsPublisher.publishNews(count);
        return "Published " + count + " test news items via Processor";
    }
}
