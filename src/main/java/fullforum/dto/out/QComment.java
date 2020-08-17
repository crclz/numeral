package fullforum.dto.out;

import fullforum.data.models.Comment;
import lombok.Data;
import lombok.Getter;
import org.modelmapper.ModelMapper;

@Data
public class QComment extends BaseQDto {
    private Long documentId;
    private Long userId;
    private Long thumbCount;
    private String content;

    // 参考 QArticle
    public Quser user;

    public static QComment convert(Comment c, Quser u, ModelMapper mapper){
        var qComment = mapper.map(c, QComment.class);
        qComment.setUser(u);
        return qComment;
    }
}
