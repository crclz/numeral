package fullforum.data.repos;

import fullforum.data.models.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership>findAllByTeamId(Long teamId);

    void deleteAllByTeamId(Long teamId);

    Membership findByUserIdAndTeamId(Long userId,Long teamId);
}
