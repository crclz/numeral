package fullforum.data.repos;

import fullforum.data.models.Thumb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThumbRepository extends JpaRepository<Thumb, Long> {
    public Thumb findByUserIdAndTargetId(long userId, long targetId);
}
