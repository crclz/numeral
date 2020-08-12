package fullforum.dto.in;

import javax.validation.constraints.NotNull;

public class PatchTeamRequestModel {
    @NotNull
    public boolean agree;

    public PatchTeamRequestModel(@NotNull boolean agree) {
        this.agree = agree;
    }
}
