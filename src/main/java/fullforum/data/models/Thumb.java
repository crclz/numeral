package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;

import javax.persistence.Entity;


@Entity
public class Thumb extends RootEntity {
    @Getter
    private long userId;

    @Getter
    private long targetId;


    @Getter
    private TargetType type;

    protected Thumb() {}

    public Thumb(long id, long userId, long targetId, TargetType type) {
        super(id);
        this.userId = userId;
        this.targetId = targetId;
        this.type = type;
    }

}
