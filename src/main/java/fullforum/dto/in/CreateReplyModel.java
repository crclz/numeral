package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CreateReplyModel {
    @NotNull
    public long commentId;

    @NotNull
    public long targetUserId;

    @NotNull
    @Size(min = 1, max = 1024)
    public String content;
}
