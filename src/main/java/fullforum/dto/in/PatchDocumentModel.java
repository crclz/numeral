package fullforum.dto.in;

import fullforum.data.models.Access;

public class PatchDocumentModel {
    public String data;
    public String title;
    public String description;
    public Long teamId;
    public Boolean isAbandoned;

    public Access publicDocumentAccess;
    public Access publicCommentAccess;
    public Boolean publicCanShare;

    public Access teamDocumentAccess;
    public Access teamCommentAccess;
    public Boolean teamCanShare;
}
