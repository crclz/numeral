package fullforum.dto.out;

import fullforum.data.models.Access;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class UserPermission {
    public long userId;

    public Access documentAccess;

    public Access commentAccess;

    public boolean canShare;



}
