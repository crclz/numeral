package fullforum.dto.in;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateTeamModel {

    @NotNull
    @Size(min = 1, max = 16)
    public String name;

    @NotNull
    @Size(max = 400)
    public String description;

    public CreateTeamModel(@NotNull @Size(min = 1, max = 16) String name, @NotNull @Size(max = 400) String description) {
        this.name = name;
        this.description = description;
    }
}
