package fullforum.data.repos;

import fullforum.data.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByCreatorId(Long creatorId);

    List<Document> findAllByTeamId(Long TeamId);

    List<Document> findAllByIsAbandoned(Boolean isAbandoned);

}
