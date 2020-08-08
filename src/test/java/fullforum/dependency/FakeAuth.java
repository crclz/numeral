package fullforum.dependency;

import fullforum.services.IAuth;

/**
 * （用于测试）将Auth给fake掉
 * 这样就不需要构造cookie来设置用户身份
 */
public class FakeAuth implements IAuth {

    /**
     * -1 为 未登录
     */
    private long realUserId = -1;

    public long getRealUserId() {
        return realUserId;
    }

    /**
     * 设置用户身份
     *
     * @param realUserId
     */
    public void setRealUserId(long realUserId) {
        this.realUserId = realUserId;
    }


    @Override
    public boolean isLoggedIn() {
        return realUserId != -1;
    }

    @Override
    public long userId() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        } else {
            return realUserId;
        }
    }
}
