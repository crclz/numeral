package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageModel {
    @NotNull
    public Long sendId; //系统通知则设置该值为-1

    @NotNull
    public Long receiverId;

    public String title;

    public String content;

    public String link;


}
