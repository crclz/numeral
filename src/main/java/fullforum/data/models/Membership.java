package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.lang.reflect.Member;

@Entity
public class Membership extends RootEntity {
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long teamId;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long userId;

    protected Membership() {

    }

    public Membership(long id, Long teamId, Long userId) {
        super(id);
        setTeamId(teamId);
        setUserId(userId);
    }
}