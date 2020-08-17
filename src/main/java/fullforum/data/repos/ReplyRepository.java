package fullforum.data.repos;

import fullforum.data.models.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    public void deleteAllByCommentId(long commentId);

}
