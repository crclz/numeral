package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.*;
import fullforum.data.repos.*;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateCommentModel;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CommentControllerTest extends BaseTest{
    @Autowired
    CommentsController commentsController;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    ThumbRepository thumbRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    FakeAuth auth;

    //test createComment

    @Test
    void creatComment_throw_UnauthorizedException_when_user_is_not_log_in() {
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
    void creatComment_throw_ForbidException_when_user_not_in_team_of_document_and_is_not_creator() {
        auth.setRealUserId(3333333);
        var document = new Document(2, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicCommentAccess(Access.Read);
        document.setTeamId(45L);
        documentRepository.save(document);
        var model = new CreateCommentModel(2L, "hahaha");

        assertThrows(ForbidException.class, () -> commentsController.createComment(model));

    }

    @Test
    void creatComment_throw_ForbidException_when_document_CommentAccess_is_not_ReadWrite() {
        auth.setRealUserId(3333333);
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







    //test removeComment

    @Test
    void removeComment_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> commentsController.removeComment(1L));
    }

    @Test
    void removeComment_throw_NotFoundException_when_comment_is_not_exist() {
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
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> commentsController.getCommentById(2L));
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

        var thumb = new Thumb(99, 1, cid.id, TargetType.Comment);
        thumbRepository.save(thumb);

        var qComment = commentsController.getCommentById(cid.id);
        assertNotNull(qComment);
        assertEquals(qComment.getContent(), model.content);
        assertEquals(qComment.getDocumentId(), model.documentId);
        assertEquals(qComment.getUserId(), auth.userId());
        assertNotNull(qComment.getMyThumb());


    }
    //test getComments

    @Test
    void getComments_throw_ForbidException_when_user_have_no_permission() {
        auth.setRealUserId(1);
        var team = new Team(999L, 21312L, "Dsadas", "dadsda");
        teamRepository.save(team);

        var membership = new Membership(12313L, 999L, 1L);
        membershipRepository.save(membership);

        var document1 = new Document(2, 4, "hahah",  "model1.description", "model1.data");
        var document2 = new Document(3, 5, "hahah",  "model1.description", "model1.data");
        var document3 = new Document(4, 6, "hahah",  "model1.description", "model1.data");
        var document4 = new Document(5, 7, "hahah",  "model1.description", "model1.data");

        document1.setPublicCommentAccess(Access.ReadWrite);
        document2.setPublicCommentAccess(Access.ReadWrite);
        document3.setPublicCommentAccess(Access.ReadWrite);
        document4.setPublicCommentAccess(Access.ReadWrite);

        document1.setPublicCommentAccess(Access.None);
        document2.setTeamId(999L);
        document2.setTeamCommentAccess(Access.None);

        documentRepository.save(document1);
        documentRepository.save(document2);
        documentRepository.save(document3);
        documentRepository.save(document4);

        var comment1 = new Comment(100L, 2L, 1L, "dasdadad");
        var comment2 = new Comment(101L, 4L, 1L, "dasdadad");
        var comment3 = new Comment(102L, 3L, 1L, "dasdadad");
        var comment4 = new Comment(103L, 2L, 2L, "dasdadad");
        var comment5 = new Comment(104L, 4L, 2L, "dasdadad");

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
        commentRepository.save(comment4);
        commentRepository.save(comment5);


        var docId1 = 2L;
        var userId = 1L;

        assertThrows(ForbidException.class, () -> commentsController.getComments(docId1, userId));

        var docId2 = document2.getId();
        assertThrows(ForbidException.class, () -> commentsController.getComments(docId2, userId));

    }

    @Test
    void getComments_return_list_of_comment_infos_when_all_ok(){
        auth.setRealUserId(1);
        var team = new Team(999L, 21312L, "Dsadas", "dadsda");
        teamRepository.save(team);

        var membership = new Membership(1231L, 999L, 1L);
        membershipRepository.save(membership);


        var document1 = new Document(2, 4, "hahah",  "model1.description", "model1.data");
        var document2 = new Document(3, 5, "hahah",  "model1.description", "model1.data");
        var document3 = new Document(4, 6, "hahah",  "model1.description", "model1.data");
        var document4 = new Document(5, 7, "hahah",  "model1.description", "model1.data");

        document1.setPublicCommentAccess(Access.ReadWrite);
        document2.setPublicCommentAccess(Access.ReadWrite);
        document3.setPublicCommentAccess(Access.ReadWrite);
        document4.setPublicCommentAccess(Access.ReadWrite);

        document1.setPublicCommentAccess(Access.Read);
        document2.setTeamId(999L);

        documentRepository.save(document1);
        documentRepository.save(document2);
        documentRepository.save(document3);
        documentRepository.save(document4);

        var comment1 = new Comment(100L, 2L, 1L, "dasdadad");
        var comment2 = new Comment(101L, 4L, 1L, "dasdadad");
        var comment3 = new Comment(102L, 3L, 1L, "dasdadad");
        var comment4 = new Comment(103L, 2L, 2L, "dasdadad");
        var comment5 = new Comment(104L, 4L, 2L, "dasdadad");

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
        commentRepository.save(comment4);
        commentRepository.save(comment5);


        var docId1 = 2L;
        var userId = 1L;
        var thumb = new Thumb(99, 1, 100L, TargetType.Comment);
        thumbRepository.save(thumb);

        var comments1 = commentsController.getComments(docId1, userId);

        assertNotNull(comments1);

        for (var comment : comments1) {
            assertEquals(comment.getUserId(), userId);
            assertEquals(comment.getDocumentId(), docId1);
            assertNotNull(comment.getMyThumb());

        }
        var docId2 = document2.getId();
        var comments2 = commentsController.getComments(docId2, null);

        assertNotNull(comments2);

        for (var comment : comments2) {
            assertEquals(comment.getUserId(), userId);
            assertEquals(comment.getDocumentId(), docId2);
        }
    }






}
