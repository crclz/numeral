package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public class DocumentControllerTest extends BaseTest {

    @Autowired
    DocumentController documentController;

    @Autowired
    Snowflake snowflake;

    @Autowired
    FakeAuth auth;

    @Autowired
    MockMvc mockMvc;



    @Test
    void createDocument_return_id_and_update_db_when_all_ok() {
        CreateDocumentModel
    }
    @Test
    void createDocument_return_id_and_update_db_when_all_ok() {
        CreateDocumentModel
    }
    @Test
    void createDocument_return_id_and_update_db_when_all_ok() {
        CreateDocumentModel
    }

    @Test
    void createDocument_return_id_and_update_db_when_all_ok() {
        CreateDocumentModel
    }

}


