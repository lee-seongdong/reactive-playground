package toy.lsd.playground.p3news;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class NewsSubscriber {
    private static final int MAILBOX_SIZE = 5;
    private final BlockingQueue<News> mailbox = new LinkedBlockingQueue<>(MAILBOX_SIZE);
    private Disposable subscription;
    private String subscriberId;

    public NewsSubscriber() {
        this.subscriberId = "subscriber-" + System.currentTimeMillis();
    }

    // 구독 기능
    public void subscribe(Observable<News> newsStream) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        
        subscription = newsStream.subscribe(
            news -> {
                if (mailbox.remainingCapacity() > 0) {
                    mailbox.offer(news);
                    System.out.println(subscriberId + " received: " + news.summary());
                } else {
                    System.out.println(subscriberId + " mailbox is full, dropping news: " + news.summary());
                }
            },
            error -> System.err.println(subscriberId + " error: " + error.getMessage()),
            () -> System.out.println(subscriberId + " subscription completed")
        );
    }

    // 뉴스 요청 기능 (사서함 크기를 초과하지 않는 만큼만)
    public int requestNews(int requestCount) {
        int availableSpace = mailbox.remainingCapacity();
        int actualRequest = Math.min(requestCount, availableSpace);
        System.out.println(subscriberId + " requesting " + actualRequest + " news items (requested: " + requestCount + ", available space: " + availableSpace + ")");
        return actualRequest;
    }

    // 구독 취소 기능
    public void unsubscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            System.out.println(subscriberId + " unsubscribed");
        }
    }

    // 사서함에서 뉴스 읽기
    public News readNews() {
        return mailbox.poll();
    }

    // 사서함 상태 확인
    public int getMailboxSize() {
        return mailbox.size();
    }

    public int getAvailableSpace() {
        return mailbox.remainingCapacity();
    }

    public String getSubscriberId() {
        return subscriberId;
    }
} 