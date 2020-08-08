package fullforum.data.models;

import fullforum.data.RootEntity;

import javax.persistence.Entity;

@Entity
public class Article extends RootEntity {

    private String title;
    private String text;
    private long userId;

    protected Article() {
    }

    public Article(long id, String title, String text, long userId) {
        super(id);
        setTitle(title);
        setText(text);
        setUserId(userId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException();
        }
        if (title.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        if (title.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.text = text;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
