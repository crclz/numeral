package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.cfg.NotYetImplementedException;

import javax.persistence.Entity;

@Entity
public class JoinRequest extends RootEntity {
    @Getter
    private Long userId;

    @Getter
    private Long teamId;

    @Getter
    private boolean isHandled = false;

    @Getter
    private boolean isAgree = false;

    protected JoinRequest() {

    }

    public JoinRequest(long id, long userId, long teamId) {
        super(id);
        this.userId = userId;
        this.teamId = teamId;
    }

    public void handle(boolean agree) {
        if (isHandled) {
            throw new IllegalStateException("Request has already been handled");
        }
        isHandled = true;

        isAgree = agree;
    }
}
