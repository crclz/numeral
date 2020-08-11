package fullforum.dto.out;

import lombok.Data;
import lombok.Getter;

@Data
public class QComment extends BaseQDto {
    private Long documentId;
    private Long userId;
    private String content;

    // 参考 QArticle
    public Quser user;
}
