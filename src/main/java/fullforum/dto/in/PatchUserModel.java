package fullforum.dto.in;

import javax.validation.constraints.Size;

public class PatchUserModel {
    @Size(min = 6, max = 32)
    public String password;

    public PatchUserModel() {
    }

    public PatchUserModel(@Size(min = 6, max = 32) String password) {
        this.password = password;
    }


}
