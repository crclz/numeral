package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;

import javax.persistence.Entity;

@Entity
public class Recommendation extends RootEntity {
    @Getter
    private long receiverId;

    @Getter
    private long senderId;

    @Getter
    private RecommendationType type;

    protected Recommendation() {
    }

    public Recommendation(long id, long receiverId, long senderId) {
        super(id);

        this.receiverId = receiverId;
        this.senderId = senderId;
    }
}
