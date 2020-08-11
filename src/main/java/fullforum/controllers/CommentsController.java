package fullforum.controllers;

import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QComment;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentsController {

    @PostMapping
    public IdDto createComment(@RequestBody CreateCommentModel model) {
        throw new NotYetImplementedException();
    }

    @DeleteMapping("{id}")
    public void removeComment(@PathVariable long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping("{id}")
    public QComment getCommentById(@PathVariable long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping
    public List<QComment> getComments(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Long userId
    ) {
        throw new NotYetImplementedException();
    }
}
