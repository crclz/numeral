package fullforum.controllers;

import fullforum.data.models.Document;
import fullforum.data.models.Favorite;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.FavoriteRepository;
import fullforum.dto.out.IdDto;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import io.swagger.annotations.ApiOperation;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/favorites")
@Validated
public class FavoritesController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FavoriteRepository favoriteRepository;


    @PostMapping
    public IdDto createFavorite(@RequestParam Long documentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            throw new NotFoundException("文档不存在");
        }
        var favorite = new Favorite(snowflake.nextId(), auth.userId(), documentId);
        favoriteRepository.save(favorite);
        return new IdDto(favorite.getId());
    }

    @DeleteMapping("{id}")
    public void removeFavorite(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var favorite = favoriteRepository.findById(id).orElse(null);
        if (favorite == null) {
            throw new NotFoundException("收藏记录不存在");
        }
        favoriteRepository.deleteById(id);
    }

    // 图省事，就把Favorite当作返回值，不去用新的dto了
    @GetMapping("{id}")
    public Favorite getFavoriteById(@PathVariable Long id) {
        return favoriteRepository.findById(id).orElse(null);
    }

    @ApiOperation("获取当前用户对于某document的favorite。如果无，则返回null")
    @GetMapping("find-by-documentId")
    public Favorite getFavoriteByDocumentId(@RequestParam Long documentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        return favoriteRepository.findByUserIdAndDocumentId(auth.userId(), documentId);
    }
}
