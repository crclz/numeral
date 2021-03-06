package fullforum.dto.out;

import fullforum.data.models.Message;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class QMessage extends BaseQDto{
    private long senderId;

    private long receiverId;

    private String title;

    private String content;

    private String link;

    private boolean haveRead = false;

    private Quser sender;

    public static QMessage convert(Message message, Quser sender, ModelMapper mapper) {
        QMessage qMessage = mapper.map(message, QMessage.class);
        qMessage.setSender(sender);
        return qMessage;
    }

}
