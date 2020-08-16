package fullforum.dto.out;

import fullforum.data.models.Message;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data
public class QMessage extends BaseQDto{
    public Long senderId;

    public Long receiverId;

    public String title;

    public String content;

    public String link;

    public boolean haveRead = false;

    public Quser sender;

    public static QMessage convert(Message message, Quser sender, ModelMapper mapper) {
        QMessage qMessage = mapper.map(message, QMessage.class);
        qMessage.setSender(sender);
        return qMessage;
    }

}
