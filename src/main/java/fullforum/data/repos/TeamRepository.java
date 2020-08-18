package fullforum.data.repos;

import fullforum.data.models.Team;
import fullforum.data.models.Thumb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    public Team findByName(String name);
}
