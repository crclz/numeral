package fullforum.data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RootEntity {
    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public RootEntity() {
    }

    public RootEntity(long id) {
        this.id = id;
    }
}
