package fullforum.data.repos;

import fullforum.data.models.TeamRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRequestRepository extends JpaRepository<TeamRequest, Long> {
    TeamRequest findByUserIdAndTeamIdAndIsHandled(Long userId, Long teamId, Boolean handled);
}
