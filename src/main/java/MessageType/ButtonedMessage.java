package MessageType;

import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class ButtonedMessage {
    private MessageType messageType;
    protected String text;
    protected Message message;
    private int currentPage;
    protected List<String> componentIds = new ArrayList<>();
    protected List<String> pages = new ArrayList<>();

    public ButtonedMessage(String text) {
        this.text = text;
    }

    public ButtonedMessage(List<String> pages) {
        this.pages = pages;
    }

    public ButtonedMessage(String text, List<String> componentIds, MessageType messageType) {
        this.text = text;
        this.componentIds = componentIds;
        this.messageType = messageType;
    }

    public ButtonedMessage(String text, List<String> componentIds, MessageType messageType, List<String> pages) {
        this.text = text;
        this.componentIds = componentIds;
        this.messageType = messageType;
        this.pages = pages;
    }

    public String getPageByGuild(String guildPageToFind) {
        return pages.get(componentIds.indexOf(guildPageToFind));
    }

    public String getPage(int index) {
        return pages.get(index);
    }

    public List<String> getPages() {
        return pages;
    }

    public int pageCount() {
        return pages.size();
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getText() {
        return text;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Message getMessage() {
        return message;
    }

    public List<String> getComponentIds() {
        return componentIds;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
