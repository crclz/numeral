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
    @Setter(AccessLevel.PRIVATE)
    private Long userId;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long teamId;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private boolean isHandled;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private boolean isRejected;

    protected JoinRequest() {

    }

    public JoinRequest(long id, Long userId, Long teamId) {
        super(id);

        setUserId(userId);
        setTeamId(teamId);
    }

    public void handle() {
        throw new NotYetImplementedException();
    }
}
