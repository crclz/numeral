package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserModel {
    @NotNull
    @Size(min = 3, max = 16)
    public String username;

    @NotNull
    @Size(min = 6, max = 32)
    public String password;

    @NotNull
    @Size(max = 32)
    public String description;
}
