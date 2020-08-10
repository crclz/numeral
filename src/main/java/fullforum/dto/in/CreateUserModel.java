package fullforum.dto.in;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
