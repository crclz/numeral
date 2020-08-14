package fullforum.dto.in;

import fullforum.data.models.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageModel {

    @NotNull
    public Long receiverId;

    @NotNull
    public MessageType type;

    public String title;

    public String content;
}
