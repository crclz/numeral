package fullforum.data.repos;

import fullforum.data.models.ViewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewRecordRepository extends JpaRepository<ViewRecord, Long> {
    public ViewRecord findByDocumentIdAndUserId(Long documentId, Long userId);

}
