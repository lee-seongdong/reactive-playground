package toy.lsd.playground.p3news;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class NewsPublisher {
    private final PublishProcessor<News> newsProcessor = PublishProcessor.create();
    private final Random random = new Random();
    
    // 구독자가 요청한 만큼 뉴스 발행
    public void publishNews(int count) {
        Observable.range(0, count)
                .delay(500, TimeUnit.MILLISECONDS) // 0.5초 간격으로 발행
                .map(i -> generateNews())
                .subscribe(news -> {
                    newsProcessor.onNext(news);
                    System.out.println("Published: " + news.summary());
                });
    }
    
    // 뉴스 스트림 제공 (Processor를 Observable로 변환)
    public Observable<News> getNewsStream() {
        return newsProcessor.toObservable();
    }
    
    // Processor를 직접 반환 (Reactive Streams 호환)
    public PublishProcessor<News> getNewsProcessor() {
        return newsProcessor;
    }
    
    // 랜덤 뉴스 생성
    private News generateNews() {
        String title = "Breaking News " + System.currentTimeMillis();
        String content = generateRandomText(100); // 100자 랜덤 텍스트
        String summary = summarizeText(content, 10); // 10자로 요약
        
        return new News(title, content, summary);
    }
    
    // 랜덤 텍스트 생성 (100자)
    private String generateRandomText(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return sb.toString();
    }
    
    // 텍스트 요약 (임의 위치에서 substring)
    private String summarizeText(String text, int summaryLength) {
        if (text.length() <= summaryLength) {
            return text;
        }
        
        int startIndex = random.nextInt(text.length() - summaryLength);
        return text.substring(startIndex, startIndex + summaryLength);
    }
}
