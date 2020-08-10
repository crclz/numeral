package fullforum.data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RootEntity {
    @Id
    private Long id;

    private Long createdAt;

    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public RootEntity() {
        createdAt = System.currentTimeMillis();
    }

    public RootEntity(long id) {
        this.id = id;
        createdAt = System.currentTimeMillis();
    }

    public void updatedAtNow() {
        updatedAt = System.currentTimeMillis();
    }
}
