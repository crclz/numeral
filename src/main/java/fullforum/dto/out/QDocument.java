package fullforum.dto.out;

import fullforum.data.models.Access;
import fullforum.data.models.Document;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

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

    public static QDocument convert(Document document, ModelMapper mapper) {
        if (document == null) {
            return null;
        }

        return mapper.map(document, QDocument.class);
    }


}
