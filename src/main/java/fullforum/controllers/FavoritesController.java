package fullforum.controllers;

import fullforum.data.models.Favorite;
import fullforum.dto.out.IdDto;
import io.swagger.annotations.ApiOperation;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Validated
public class FavoritesController {

    @PostMapping
    public IdDto createFavorite(@RequestParam Long documentId) {
        throw new NotYetImplementedException();
    }

    @DeleteMapping("{id}")
    public void removeFavorite(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    // 图省事，就把Favorite当作返回值，不去用新的dto了
    @GetMapping("{id}")
    public Favorite getFavoriteById(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    @ApiOperation("获取当前用户对于某document的favorite。如果无，则返回null")
    @GetMapping("find-by-documentId")
    public Favorite getFavoriteByDocumentId(@RequestParam Long documentId) {
        throw new NotYetImplementedException();
    }
}
