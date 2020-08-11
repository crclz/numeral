package fullforum.dto.in;

import javax.validation.constraints.Size;

public class CreateDocumentModel {

    @Size(min = 1, max = 25)
    public String title;

    @Size(max = 140)
    public String description;

    public String data;
}