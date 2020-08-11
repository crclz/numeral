package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Access;
import fullforum.data.models.Comment;
import fullforum.data.models.Document;
import fullforum.data.models.Favorite;
import fullforum.data.repos.CommentRepository;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.FavoriteRepository;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.dto.in.PatchDocumentModel;
import fullforum.dto.out.QComment;
import fullforum.dto.out.QDocument;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentControllerTest extends BaseTest{
    @Autowired
    CommentsController commentsController;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FakeAuth auth;

    //test createComment

    @Test
    void creatFComment_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateCommentModel(1L, "hahaha");
        assertThrows(UnauthorizedException.class, () -> commentsController.createComment(model));
    }

    @Test
    void creatComment_throw_NotFoundException_when_document_is_not_exist() {
        auth.setRealUserId(1);
        var model = new CreateCommentModel(1L, "hahaha");
        assertThrows(NotFoundException.class, () -> commentsController.createComment(model));
    }

    @Test
    void creatComment_throw_ForbidException_when_document_CommentAccess_is_not_ReadWrite() {
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.Read);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");

        assertThrows(ForbidException.class, () -> commentsController.createComment(model));

    }

    @Test
    void creatComment_return_id_and_update_db_when_all_ok(){
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.ReadWrite);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");

        var cid = commentsController.createComment(model);
        assertNotNull(cid);

        var commentInDb = commentRepository.findById(cid.id).orElse(null);

        assertNotNull(commentInDb);
        assertThat(commentInDb.getDocumentId()).isEqualTo(document.getId());
        assertThat(commentInDb.getUserId()).isEqualTo(auth.userId());
    }


    //test removeFavorite

    @Test
    void removeComment_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> commentsController.removeComment(1L));
    }

    @Test
    void removeComment_throw_NotFoundException_when_favorite_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> commentsController.removeComment(1L));
    }

    @Test
    void removeComment_throw_ForbidException_when_user_is_not_creator() {
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.ReadWrite);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");
        var cid = commentsController.createComment(model);

        var commentInDb = commentRepository.findById(cid.id).orElse(null);
        assertNotNull(commentInDb);
        auth.setRealUserId(2L);
        assertThrows(ForbidException.class, () -> commentsController.removeComment(commentInDb.getId()));
    }

    @Test
    void removeComment_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.ReadWrite);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");
        var cid = commentsController.createComment(model);

        var commentInDb = commentRepository.findById(cid.id).orElse(null);
        assertNotNull(commentInDb);
        commentsController.removeComment(cid.id);
        commentInDb = commentRepository.findById(cid.id).orElse(null);
        assertNull(commentInDb);
    }

    //test getCommentById
    @Test
    void getCommentById_return_null_when_comment_not_exist() {
        var comment = commentsController.getCommentById(1L);
        assertThat(comment).isNull();
    }

    @Test
    void getCommentById_return_comment_when_comment_exist() {
        auth.setRealUserId(1);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.ReadWrite);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");

        var cid = commentsController.createComment(model);
        assertNotNull(cid);

        var qComment = commentsController.getCommentById(cid.id);
        assertNotNull(qComment);
        assertEquals(qComment.getContent(), model.content);
        assertEquals(qComment.getDocumentId(), model.documentId);
        assertEquals(qComment.getUserId(), auth.userId());

    }

    @Test
    void getComments_return_list_of_comment_infos_when_comment_exist(){
        auth.setRealUserId(1);
        var document1 = new Document(2, 4, "hahah",  "model1.description", "model1.data");
        var document2 = new Document(3, 5, "hahah",  "model1.description", "model1.data");
        var document3 = new Document(4, 6, "hahah",  "model1.description", "model1.data");
        var document4 = new Document(5, 7, "hahah",  "model1.description", "model1.data");

        document1.setPublicCommentAccess(Access.ReadWrite);
        document2.setPublicCommentAccess(Access.ReadWrite);
        document3.setPublicCommentAccess(Access.ReadWrite);
        document4.setPublicCommentAccess(Access.ReadWrite);

        documentRepository.save(document1);
        documentRepository.save(document2);
        documentRepository.save(document3);
        documentRepository.save(document4);

        var model1 = new CreateCommentModel(2L, "hahaha");
        var model2 = new CreateCommentModel(4L, "hahaha");
        var model3 = new CreateCommentModel(3L, "hahaha");
        var cid1 = commentsController.createComment(model1);
        var cid2 = commentsController.createComment(model2);
        var cid3 = commentsController.createComment(model3);
        auth.setRealUserId(2L);
        var model4 = new CreateCommentModel(2L, "hahaha");
        var model5 = new CreateCommentModel(4L, "hahaha");
        var cid4 = commentsController.createComment(model4);
        var cid5 = commentsController.createComment(model5);

        var docId = 2L;
        var userId = 1L;

        var comments = commentsController.getComments(docId, userId);

        assertNotNull(comments);

        for (var comment : comments) {
            assertEquals(comment.getUserId(), userId);
            assertEquals(comment.getDocumentId(), docId);
        }
    }






}
