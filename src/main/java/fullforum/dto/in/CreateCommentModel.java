package fullforum.dto.in;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateCommentModel {
    @NotNull
    public Long documentId;

    @NotNull
    @Size(min = 1, max = 140)
    public String content;

    public CreateCommentModel(@NotNull Long documentId, @NotNull @Size(min = 1, max = 140) String content) {
        this.documentId = documentId;
        this.content = content;
    }
}
