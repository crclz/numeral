package fullforum.data.repos;

import fullforum.data.models.ELock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ELockRepository extends JpaRepository<ELock, Long> {
    public ELock findELockByDocumentId(Long documentId);
}
