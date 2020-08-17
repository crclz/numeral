package fullforum.data.repos;

import fullforum.data.models.TargetType;
import fullforum.data.models.Thumb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThumbRepository extends JpaRepository<Thumb, Long> {
    Thumb findByUserIdAndTargetId(long userId, long targetId);

    Thumb findByUserIdAndTargetIdAndType(long userId, long targetId, TargetType type);
}
