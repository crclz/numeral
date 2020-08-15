package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.cfg.NotYetImplementedException;

import javax.persistence.Entity;

@Entity
public class TeamRequest extends RootEntity {
    @Getter
    private long userId;

    @Getter
    private long teamId;

    @Getter
    private boolean isHandled = false;

    @Getter
    private boolean isAgree = false;

    protected TeamRequest() {

    }

    public TeamRequest(long id, long userId, long teamId) {
        super(id);
        this.userId = userId;
        this.teamId = teamId;
    }

    public void handle(boolean agree) {
        if (isHandled) {
            throw new IllegalStateException("请求已被处理");
        }
        isHandled = true;

        isAgree = agree;
    }
}
