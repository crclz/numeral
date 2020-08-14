package fullforum.data.repos;

import fullforum.data.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByReceiverIdAndHaveRead(Long receiverId, Boolean haveRead);

    List<Message> findAllByReceiverId(Long receiverId);
}
