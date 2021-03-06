package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamModel {

    @NotNull
    @Size(min = 1, max = 16)
    public String name;

    @NotNull
    @Size(max = 255)
    public String description;
}
