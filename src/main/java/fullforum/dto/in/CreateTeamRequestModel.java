package fullforum.dto.in;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// 只有一个字段，但还是做一个类。因为以后可能会添加一些其他的东西，例如加入小组的验证消息
public class CreateTeamRequestModel {
    @NotNull
    public Long teamId;
}
