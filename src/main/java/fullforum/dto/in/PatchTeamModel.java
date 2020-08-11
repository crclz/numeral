package fullforum.dto.in;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PatchTeamModel {
    @Size(min = 1, max = 16)
    public String name;

    @Size(max = 400)
    public String description;

    public PatchTeamModel(@Size(min = 1, max = 16) String name, @Size(max = 400) String description) {
        this.name = name;
        this.description = description;
    }
}
