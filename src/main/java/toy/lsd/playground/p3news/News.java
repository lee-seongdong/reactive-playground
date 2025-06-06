package toy.lsd.playground.p3news;

public record News(String title, String content, String summary) {

    @Override
    public String toString() {
        return "News{title='" + title + "', summary='" + summary + "'}";
    }
} 