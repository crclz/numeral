package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatchUserModel {
    @Size(min = 6, max = 32)
    public String password;

    @Size(max = 32)
    public String description;

    public String avatarUrl;
}
