package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Document;
import fullforum.data.models.Favorite;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.FavoriteRepository;
import fullforum.dependency.FakeAuth;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FavoriteControllerTest extends BaseTest{
    @Autowired
    FavoritesController favoritesController;

    @Autowired
    FavoriteRepository favoriteRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FakeAuth auth;

    //test createFavorite

    @Test
    void creatFavorite_throw_UnauthorizedException_when_user_is_not_log_in(){
        assertThrows(UnauthorizedException.class, () -> favoritesController.createFavorite(1L));
    }

    @Test
    void creatFavorite_throw_NotFoundException_when_document_is_not_exist(){
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> favoritesController.createFavorite(1L));
    }

    @Test
    void creatFavorite_return_id_and_update_db_when_all_ok(){
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        documentRepository.save(document);
        var fid = favoritesController.createFavorite(2L);
        assertNotNull(fid);

        Favorite favoriteInDb = favoriteRepository.findById(fid.id).orElse(null);

        assertNotNull(favoriteInDb);
        assertThat(favoriteInDb.getDocumentId()).isEqualTo(document.getId());
        assertThat(favoriteInDb.getUserId()).isEqualTo(auth.userId());
    }


    //test removeFavorite

    @Test
    void removeFavorite_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> favoritesController.removeFavorite(1L));
    }

    @Test
    void removeFavorite_throw_NotFoundException_when_favorite_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> favoritesController.removeFavorite(1L));
    }


    @Test
    void removeFavorite_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(3);
        var favorite = new Favorite(1, 2, 3);
        favoriteRepository.save(favorite);

        var favoriteInDb = favoriteRepository.findById(1L).orElse(null);
        assertNotNull(favoriteInDb);

        favoritesController.removeFavorite(1L);

        favoriteInDb = favoriteRepository.findById(1L).orElse(null);
        assertThat(favoriteInDb).isNull();
    }

    //test getFavoriteById
    @Test
    void getFavoriteById_return_null_when_favorite_not_exist() {
        var favorite = favoritesController.getFavoriteById(1L);
        assertThat(favorite).isNull();
    }

    @Test
    void getFavoriteById_return_favorite_when_favorite_exist() {
        var favoriteEntity = new Favorite(1,2,3);
        favoriteRepository.save(favoriteEntity);

        var favorite = favoritesController.getFavoriteById(1L);
        assertThat(favorite.getId()).isEqualTo(1);
        assertThat(favorite.getUserId()).isEqualTo(2);
        assertThat(favorite.getDocumentId()).isEqualTo(3);
    }

    //test getFavoriteByDocumentId
    @Test
    void getFavoriteByDocumentId_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> favoritesController.getFavoriteByDocumentId(1L));
    }

    @Test
    void getFavoriteByDocumentId_return_null_when_favorite_is_not_exist() {
        auth.setRealUserId(1);
        favoritesController.getFavoriteByDocumentId(1L);
    }

    @Test
    void getFavoriteByDocumentId_return_favorite_when_favorite_exist() {
        auth.setRealUserId(2);
        var favoriteEntity = new Favorite(1,2,3);
        favoriteRepository.save(favoriteEntity);

        var favorite = favoritesController.getFavoriteByDocumentId(3L);
        assertThat(favorite.getId()).isEqualTo(1);
        assertThat(favorite.getUserId()).isEqualTo(2);
        assertThat(favorite.getDocumentId()).isEqualTo(3);
    }






}
