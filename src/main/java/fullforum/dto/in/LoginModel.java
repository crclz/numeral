package fullforum.dto.in;

public class LoginModel {
    public String username;
    public String password;

    public LoginModel() {
    }

    public LoginModel(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
