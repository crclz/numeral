package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// 只有一个字段，但还是做一个类。因为以后可能会添加一些其他的东西，例如加入小组的验证消息
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamRequestModel {
    @NotNull
    public Long teamId;
}
