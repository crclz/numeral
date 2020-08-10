package fullforum.controllers;

import fullforum.data.models.Document;
import fullforum.data.repos.DocumentRepository;
import fullforum.dto.in.DocumentTestModel;
import fullforum.services.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@Validated// PathVariable and params auto validation
public class DocumentController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    DocumentRepository documentRepository;

    @PostMapping("update-doc-1")
    public void updateDocumentOne(@RequestBody DocumentTestModel model) {
        var doc = documentRepository.findById(1L).orElse(null);
        if (doc == null) {
            doc = new Document(1);
        }

        doc.setData(model.data);

        documentRepository.save(doc);
    }

    @GetMapping("get-doc-1")
    public Document getDocumentOne() {
        var doc = documentRepository.findById(1L).orElse(null);
        return doc;
    }
}
