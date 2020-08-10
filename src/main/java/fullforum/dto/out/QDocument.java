package fullforum.dto.out;

import fullforum.data.models.Access;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Data
public class QDocument {
    public Long id;
    public Long creatorId;
    public Long teamId;
    public boolean isAbandoned;
    public Access publicDocumentAccess;
    public Access publicCommentAccess;
    public boolean publicCanShare;
    public Access teamDocumentAccess;
    public Access teamCommentAccess;
    public boolean teamCanShare;
    public Long lastModifierId;
    public String data;
}
