package fullforum.data.repos;

import fullforum.data.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Favorite findByUserIdAndDocumentId(Long userId, Long documentId);

}
