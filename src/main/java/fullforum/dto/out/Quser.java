package fullforum.dto.out;

import fullforum.data.models.User;
import lombok.Data;
import org.modelmapper.ModelMapper;

@Data// ModelMapper需要getter和setter
public class Quser {
    public long id;
    public String username;
    public String description;
    public String avatarUrl;

    public static Quser convert(User user, ModelMapper mapper) {
        if (user == null) {
            return null;
        }

        return mapper.map(user, Quser.class);
    }
}
