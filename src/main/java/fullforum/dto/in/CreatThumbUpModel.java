package fullforum.dto.in;

import fullforum.data.models.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class CreatThumbUpModel {
    @NotNull
    public long targetId;

    @NotNull
    public TargetType targetType;
}
