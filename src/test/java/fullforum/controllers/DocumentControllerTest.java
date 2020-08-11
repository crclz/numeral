package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Access;
import fullforum.data.models.Document;
import fullforum.data.repos.DocumentRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;


public class DocumentControllerTest extends BaseTest {

    @Autowired
    DocumentController documentController;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    Snowflake snowflake;

    @Autowired
    FakeAuth auth;

    @Autowired
    MockMvc mockMvc;



    @Test
    void createDocument_throw_IllegalArgumentException_when_model_is_invalid() {

        auth.setRealUserId(1);
        var model1 = new CreateDocumentModel();
        model1.data = "hahaha";
        model1.title = "";
        model1.description = "ddddd";
        var model2 = new CreateDocumentModel();
        model2.data = "hahaha";
        model2.title = "ttt";
        model2.description = "11111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                "11111111111111111111111111111111111111111111111111111111111111111111111111111111";//160
        assertThrows(IllegalArgumentException.class, () -> documentController.createDocument(model1));
        assertThrows(IllegalArgumentException.class, () -> documentController.createDocument(model2));
    }

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
    void patchDocument_throw_ForbidException_when_user_is_not_creator_and_PublicDocumentAccess_is_not_ReadWrite() {
        auth.setRealUserId(2);
        var document = new Document(1, 1, "hahah",  "model1.description", "model1.data");
        document.setPublicDocumentAccess(Access.Read);
        documentRepository.save(document);

        var patch = new PatchDocumentModel();
        patch.description = "eeeee";

        assertThrows(ForbidException.class, () -> documentController.patchDocument(patch, 1));
    }
//

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
    void getDocumentById_return_null_when_document_not_exist() {
        var doc = documentController.getDocumentById(1L);
        assertThat(doc).isNull();
    }

    @Test
    void getDocumentById_return_document_info_when_document_exist() {

        var docEntity = new Document(1, 2, "qwqqqq", "sawqewqe", "sqdqwe");
        documentRepository.save(docEntity);

        var doc = documentController.getDocumentById(1L);

        assertThat(doc.getId()).isEqualTo(1);
        assertThat(doc.getTitle()).isEqualTo(docEntity.getTitle());
        assertThat(doc.getDescription()).isEqualTo(docEntity.getDescription());
    }


    //test getDocuments
    @Test

    void getDocuments_return_list_of_document_infos_when_document_exist() {

        var docEntity1 = new Document(1, 2, "qwqqqq", "sawqewqe", "sqdqwe");
        var docEntity2 = new Document(2, 2, "aa", "wwwqewsdqe", "sqdqsswe");
        var docEntity3 = new Document(3, 3, "dddqqqq", "ewqe", "kkk");
        var docEntity4 = new Document(4, 3, "qwerqq", "ppope", "ppp");
        var docEntity5 = new Document(5, 2, "llklklkqq", "srtqe", "oook");

        docEntity1.setTeamId(4L);
        docEntity2.setTeamId(4L);
        docEntity3.setTeamId(5L);
        docEntity4.setTeamId(4L);
        docEntity5.setIsAbandoned(true);
        documentRepository.save(docEntity1);
        documentRepository.save(docEntity2);
        documentRepository.save(docEntity3);
        documentRepository.save(docEntity4);
        documentRepository.save(docEntity5);

        var creatorId = 3L;
        var teamId = 5L;
        List<QDocument> documentList1 = documentController.getDocuments(creatorId, teamId, false, false);
        for (QDocument qDocument:documentList1) {
            assertThat(qDocument.getCreatorId()).isEqualTo(creatorId);
            assertThat(qDocument.getTeamId().longValue()).isEqualTo(teamId);
        }

        auth.setRealUserId(2);
        List<QDocument> documentList2 = documentController.getDocuments(creatorId, teamId, false, true);
        assertThat(documentList2.size()).isNotZero();
        for (QDocument qDocument:documentList2) {
            assertThat(qDocument.getCreatorId()).isEqualTo(auth.userId());
            assertThat(qDocument.isAbandoned()).isEqualTo(true);
        }
    }

}


