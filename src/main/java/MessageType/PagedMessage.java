package MessageType;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PagedMessage {
    private Message message;
    private int currentPage;
    private final List<String> pages;

    /**
     * Constructs a new PagedMessage object.
     * @param pages The pages of the message.
     */
    public PagedMessage(List<String> pages) {
        currentPage = 0;
        this.pages = pages;
    }

    /**
     * Sets message this PagedMessage relates to.
     * @param message The message to relate to.
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Gets the message this object relates to.
     * @return The message is relates to.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Gets the specific page to display.
     * @param index The page to display.
     * @return The page to display.
     */
    public String getPage(int index) {
        try {
            return pages.get(index);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return "No data available";
        }
    }

    /**
     * How many pages the message has.
     * @return The number of pages.
     */
    public int pageCount() {
        return pages.size();
    }

    /**
     * Gets what page is currently being viewed.
     * @return The page index currently being viewed.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets which current page is being viewed.
     * @param currentPage The page being viewed.
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
