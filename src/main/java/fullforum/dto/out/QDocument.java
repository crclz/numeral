package fullforum.dto.out;

import fullforum.data.models.Access;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Data
public class QDocument extends BaseQDto {
    private Long creatorId;
    private Long teamId;// 可空
    private String title;
    private String description;
    private boolean isAbandoned;
    private Access publicDocumentAccess;
    private Access publicCommentAccess;
    private boolean publicCanShare;
    private Access teamDocumentAccess;
    private Access teamCommentAccess;
    private boolean teamCanShare;
    private Long lastModifierId;
    private String data;
    private int modifyCount;
}
