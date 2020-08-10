package fullforum.dto.in;

import javax.validation.constraints.Size;

public class PatchUserModel {
    @Size(min = 6, max = 32)
    public String password;

    @Size(max = 32)
    public String description;

    public String avatarUrl;

    public PatchUserModel() {
    }


}
