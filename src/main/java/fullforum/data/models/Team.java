package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Team extends RootEntity {
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long leaderId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    protected Team() {

    }

    public Team(Long leaderId, String name, String description) {
        this.leaderId = leaderId;
        setName(name);
        setDescription(description);
    }
}
