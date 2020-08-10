package fullforum.dto.in;

import fullforum.data.models.Access;

import javax.validation.constraints.Size;

public class PatchDocumentModel {
    public String data;
    @Size(min = 1, max = 25)
    public String title;

    @Size(max = 140)
    public String description;
    public Long teamId;
    public boolean isAbandoned;

    public Access publicDocumentAccess;
    public Access publicCommentAccess;
    public Boolean publicCanShare;

    public Access teamDocumentAccess;
    public Access teamCommentAccess;
    public Boolean teamCanShare;
}
