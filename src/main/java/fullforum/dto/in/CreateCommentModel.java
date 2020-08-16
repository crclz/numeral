package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentModel {
    @NotNull
    public Long documentId;

    @NotNull
    @Size(min = 1, max = 1024)
    public String content;
}
