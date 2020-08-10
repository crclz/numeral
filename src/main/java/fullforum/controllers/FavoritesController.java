package fullforum.controllers;

import fullforum.data.models.Favorite;
import io.swagger.annotations.ApiOperation;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@Validated
public class FavoritesController {

    @PutMapping
    public void putFavorite(@RequestParam Long documentId, boolean isFavorite) {
        throw new NotYetImplementedException();
    }
}
