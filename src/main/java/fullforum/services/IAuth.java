package fullforum.services;

/**
 * 用户身份验证的接口
 */
public interface IAuth {
    boolean isLoggedIn();

    long userId();
}
