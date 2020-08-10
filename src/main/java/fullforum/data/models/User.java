package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class User extends RootEntity {
    @Getter
    private String username;

    @Getter
    private String password;

    @Getter
    private String description;

    @Getter
    private String avatarUrl;

    protected User() {
    }

    public User(long id, String username, String password, String description, String avatarUrl) {
        super(id);
        setUsername(username);
        setPassword(password);
        setDescription(description);
        setAvatarUrl(avatarUrl);
    }

    public void setUsername(String username) {
        if (username == null) {
            throw new NullPointerException();
        }
        if (!(username.length() >= 3 && username.length() <= 16)) {
            throw new IllegalArgumentException("username length should in [3,16]");
        }

        this.username = username;
    }

    public void setPassword(String password) {
        if (password == null) {
            throw new NullPointerException();
        }
        if (!(password.length() >= 6 && password.length() <= 32)) {
            throw new IllegalArgumentException();
        }
        this.password = password;
    }

    public boolean checkPassword(String passwordToCheck) {
        return password.equals(passwordToCheck);
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new NullPointerException();
        }
        if (description.length() > 120) {
            throw new IllegalArgumentException("Description should shorter than 120");
        }
        this.description = description;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}