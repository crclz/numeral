package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentModel {

    @Size(min = 1, max = 25)
    public String title;

    @Size(max = 140)
    public String description;

    public String data;
}