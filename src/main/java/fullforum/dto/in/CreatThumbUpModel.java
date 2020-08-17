package fullforum.dto.in;

import fullforum.data.models.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatThumbUpModel {
    public long targetId;

    public TargetType targetType;


}
