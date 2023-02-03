import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PagedMessage {
    private Message message;
    private int currentPage;
    private final List<String> pages;

    public PagedMessage(List<String> pages) {
        currentPage = 0;
        this.pages = pages;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public String getPage(int index) {
        try {
            return pages.get(index);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return "No tracked guilds";
        }
    }

    public int pageCount() {
        return pages.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
