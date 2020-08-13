package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Access;
import fullforum.data.models.Document;
import fullforum.data.models.Membership;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.MembershipRepository;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.dto.in.PatchDocumentModel;
import fullforum.dto.out.QDocument;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;


public class DocumentControllerTest extends BaseTest {

    @Autowired
    DocumentController documentController;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    Snowflake snowflake;

    @Autowired
    FakeAuth auth;

    @Autowired
    MockMvc mockMvc;



    @Test
    void createDocument_throw_UnauthorizedException_when_user_is_not_login() {
        var model1 = new CreateDocumentModel();
        model1.data = "hahaha";
        model1.title = "asa";
        model1.description = "ddddd";
        assertThrows(UnauthorizedException.class, () -> documentController.createDocument(model1));
    }

    @Test
    void createDocument_return_id_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var model1 = new CreateDocumentModel();
        model1.data = "hahaha";
        model1.title = "asd";
        model1.description = "ddddd";

        var doc = documentController.createDocument(model1);
        var docId = doc.id;

        documentRepository.flush();

        var documents = documentRepository.findAll();

        var docInDb = documentRepository.findById(docId).orElse(null);

        assertNotNull(docInDb);


        assertTrue(documents.size() > 0);
        assertEquals(docId, docInDb.getId());
        assertEquals(model1.title, docInDb.getTitle());
        assertEquals(model1.description, docInDb.getDescription());
        assertEquals(model1.data, docInDb.getData());
        assertEquals(1, docInDb.getCreatorId());


    }


    //test patchDocument

    @Test
    void patchDocument_throw_UnauthorizedException_when_user_is_not_login() {
        var patch = new PatchDocumentModel();
        patch.description = "eeeee";
        assertThrows(UnauthorizedException.class, () -> documentController.patchDocument(patch, 1));
    }
    @Test
    void patchDocument_throw_ForbidException_when_user_is_not_creator_and_not_in_team_of_document() {
        auth.setRealUserId(2);
        var document = new Document(1, 1, "hahah",  "model1.description", "model1.data");
        document.setTeamId(99L);
        documentRepository.save(document);

        var patch = new PatchDocumentModel();
        patch.description = "eeeee";

        assertThrows(ForbidException.class, () -> documentController.patchDocument(patch, 1));
    }
    @Test
    void patchDocument_throw_ForbidException_when_user_is_not_creator_and_PublicDocumentAccess_is_not_ReadWrite() {
        auth.setRealUserId(2);
        var document = new Document(1, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicDocumentAccess(Access.Read);
        documentRepository.save(document);

        var patch = new PatchDocumentModel();
        patch.description = "eeeee";

        assertThrows(ForbidException.class, () -> documentController.patchDocument(patch, 1));
    }

    @Test
    void patchDocument_throw_NotFoundException_when_document_is_not_exist(){
        auth.setRealUserId(1);
        var patch = new PatchDocumentModel();
        patch.description = "eeeee";
        assertThrows(NotFoundException.class, () -> documentController.patchDocument(patch, 2));
    }
    @Test
    void patchDocument_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var document = new Document(1, 1, "hahah",  "model1.description", "model1.data");
        documentRepository.save(document);
        var patch = new PatchDocumentModel();
        patch.description = "eeeee";
        documentController.patchDocument(patch, 1);

        var docInDb = documentRepository.findById(1L).orElse(null);
        assertThat(docInDb).isNotNull();
        assertThat(docInDb.getDescription()).isEqualTo(patch.description);

    }

    //test removeDocument

    @Test
    void removeDocument_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> documentController.removeDocument(1L));
    }

    @Test
    void removeDocument_throw_NotFoundException_when_document_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> documentController.removeDocument(2L));
    }

    @Test
    void removeDocument_throw_ForbidException_when_user_is_not_creator() {
        auth.setRealUserId(1);
        var doc = new Document(2, 3, "ss", "dsadas", "dasdawd");

        documentRepository.save(doc);
        assertThrows(ForbidException.class, () -> documentController.removeDocument(2L));
    }

    @Test
    void removeDocument_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(3);
        var doc = new Document(2, 3, "ss", "dsadas", "dasdawd");
        documentRepository.save(doc);

        var docInDb = documentRepository.findById(doc.getId()).orElse(null);
        assertThat(docInDb).isNotNull();

        documentController.removeDocument(docInDb.getId());

        var docInDb1 = documentRepository.findById(docInDb.getId()).orElse(null);
        assertThat(docInDb1).isNull();
    }


    //test getDocumentById

    @Test
    void getDocumentById_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> documentController.getDocumentById(2L));
    }

    @Test
    void getDocumentById_return_null_when_document_not_exist() {
        auth.setRealUserId(1);
        var doc = documentController.getDocumentById(1L);
        assertThat(doc).isNull();
    }

    @Test
    void getDocument_throw_ForbidException_when_user_have_no_permission() {
        auth.setRealUserId(1);
        var docEntity1 = new Document(2, 3, "ss", "dsadas", "dasdawd");
        var docEntity2 = new Document(7, 8, "dasda", "dasda", "Asdddad");
        docEntity1.setTeamId(100L);
        docEntity1.setTeamDocumentAccess(Access.ReadWrite);
        docEntity2.setPublicDocumentAccess(Access.None);

        documentRepository.save(docEntity1);
        documentRepository.save(docEntity2);

        assertThrows(ForbidException.class, () -> documentController.getDocumentById(2L));
        assertThrows(ForbidException.class, () -> documentController.getDocumentById(7L));

    }


    @Test
    void getDocumentById_return_document_info_when_all_ok() {

        auth.setRealUserId(8);
        var docEntity1 = new Document(2, 3, "ss", "dsadas", "dasdawd");
        var docEntity2 = new Document(7, 8, "dasda", "dasda", "Asdddad");
        docEntity1.setTeamId(100L);
        docEntity1.setTeamDocumentAccess(Access.ReadWrite);
        docEntity2.setPublicDocumentAccess(Access.None);

        documentRepository.save(docEntity1);
        documentRepository.save(docEntity2);

        var docInDb1 = documentController.getDocumentById(7L);
        assertNotNull(docInDb1);

        auth.setRealUserId(5L);
        var membership = new Membership(1L, 100L, 5L);
        membershipRepository.save(membership);

        var docInDb2 = documentController.getDocumentById(2L);
        assertNotNull(docInDb2);
    }



    //test getDocuments

    @Test
    void getDocuments_throw_ForbidException_when_user_is_not_team_member() {
        var docEntity1 = new Document(1, 2, "qwqqqq", "sawqewqe", "sqdqwe");
        docEntity1.setTeamId(4L);
        documentRepository.save(docEntity1);

        var creatorId = 3L;
        var teamId = 4L;
        auth.setRealUserId(9);

        assertThrows(ForbidException.class, () -> documentController.getDocuments(creatorId, teamId, false, false));
    }

    @Test
    void getDocuments_return_list_of_document_infos_when_document_exist() {
        var docEntity1 = new Document(1, 2, "qwqqqq", "sawqewqe", "sqdqwe");
        var docEntity2 = new Document(2, 2, "aa", "wwwqewsdqe", "sqdqsswe");
        var docEntity3 = new Document(3, 3, "dddqqqq", "ewqe", "kkk");
        var docEntity4 = new Document(4, 3, "qwerqq", "ppope", "ppp");
        var docEntity5 = new Document(5, 3, "llklklkqq", "srtqe", "oook");

        docEntity1.setTeamId(4L);
        docEntity2.setTeamId(4L);
        docEntity3.setTeamId(4L);
        docEntity4.setTeamId(5L);
        docEntity5.setIsAbandoned(true);
        docEntity4.setPublicDocumentAccess(Access.None);
        documentRepository.save(docEntity1);
        documentRepository.save(docEntity2);
        documentRepository.save(docEntity3);
        documentRepository.save(docEntity4);
        documentRepository.save(docEntity5);

        var membership = new Membership(123L, 4L, 66L);
        membershipRepository.save(membership);

        var creatorId = 3L;
        var teamId = 4L;

        auth.setRealUserId(66);
        var documentList1 = documentController.getDocuments(creatorId, teamId, false, false);
        assertThat(documentList1.size()).isNotZero();
        for (QDocument qDocument:documentList1) {
            assertThat(qDocument.getCreatorId()).isEqualTo(creatorId);
            assertThat(qDocument.getTeamId().longValue()).isEqualTo(teamId);
        }

        auth.setRealUserId(2);
        var documentList2 = documentController.getDocuments(creatorId, null, false, false);
        assertThat(documentList2.size()).isNotZero();
        for (QDocument qDocument:documentList2) {
            assertThat(qDocument.getCreatorId()).isEqualTo(creatorId);
            assertThat(qDocument.getPublicDocumentAccess()).isNotEqualTo(Access.None);
        }

        auth.setRealUserId(3);
        var documentList3 = documentController.getDocuments(creatorId, null, false, true);
        assertThat(documentList3.size()).isNotZero();
        for (QDocument qDocument:documentList3) {
            assertThat(qDocument.getCreatorId()).isEqualTo(auth.userId());
            assertThat(qDocument.isAbandoned()).isEqualTo(true);
        }
    }

}


