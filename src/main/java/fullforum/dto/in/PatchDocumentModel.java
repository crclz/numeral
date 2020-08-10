package fullforum.dto.in;

import fullforum.data.models.Access;

public class PatchDocumentModel {
    public String data;
    public Long teamId;
    public boolean isAbandoned;

    public Access publicDocumentAccess;
    public Access publicCommentAccess;
    public Boolean publicCanShare;

    public Access teamDocumentAccess;
    public Access teamCommentAccess;
    public Boolean teamCanShare;
}
