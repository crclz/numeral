package fullforum.dto.out;

import fullforum.data.models.Reply;
import fullforum.data.models.Thumb;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class QReply extends BaseQDto {
    private long commentId;

    private long userId;

    private long targetUserId;

    private int thumbCount;

    private String content;

    private Quser user;

    private Thumb myThumb;

    public static QReply convert(Reply reply, Quser user, ModelMapper mapper, Thumb myThumb) {
        var qReply = mapper.map(reply, QReply.class);
        qReply.setUser(user);
        qReply.setMyThumb(myThumb);
        return qReply;
    }


}
