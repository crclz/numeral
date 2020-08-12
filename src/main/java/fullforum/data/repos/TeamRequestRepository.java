package fullforum.data.repos;

import fullforum.data.models.TeamRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRequestRepository extends JpaRepository<TeamRequest, Long> {
    TeamRequest findByUserIdAndTeamId(Long userId, Long teamId);
}
