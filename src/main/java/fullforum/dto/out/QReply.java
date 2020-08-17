package fullforum.dto.out;

import fullforum.data.models.Reply;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class QReply extends BaseQDto {
    private long commentId;

    private long userId;

    private long targetUserId;

    private String content;

    private Quser user;

    public static QReply convert(Reply reply, Quser user, ModelMapper mapper) {
        var qReply = mapper.map(reply, QReply.class);
        qReply.setUser(user);
        return qReply;
    }


}
