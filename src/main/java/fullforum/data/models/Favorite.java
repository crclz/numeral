package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Favorite extends RootEntity {
    @Getter
    private Long userId;

    @Getter
    private Long documentId;

    protected Favorite() {

    }

    public Favorite(long id, long userId, long documentId) {
        super(id);
        this.userId = userId;
        this.documentId = documentId;
    }
}
