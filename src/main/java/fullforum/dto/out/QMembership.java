package fullforum.dto.out;

import fullforum.data.models.Membership;
import lombok.Data;
import lombok.Getter;
import org.modelmapper.ModelMapper;

@Data
public class QMembership extends BaseQDto {
    private Long teamId;
    private Long userId;

    private Quser user;
    private QTeam team;

    public static QMembership convert(Membership membership, ModelMapper mapper, Quser quser, QTeam qTeam) {
        var qMembership = mapper.map(membership, QMembership.class);
        qMembership.setTeam(qTeam);
        qMembership.setUser(quser);
        return qMembership;
    }
}
