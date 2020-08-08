package fullforum.data.models;

import fullforum.data.RootEntity;

import javax.persistence.Entity;

@Entity
public class User extends RootEntity {
    private String username;
    private String password;

    protected User() {
    }

    public User(long id, String username, String password) {
        super(id);
        setUsername(username);
        setPassword(password);
    }


    public String getUsername() {
        return username;
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

    public String getPassword() {
        return password;
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
}