package fullforum.dto.out;

import fullforum.data.models.Team;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Data
public class QTeam {
    private Long leaderId;
    private String name;
    private String description;

    public static QTeam convert(Team team, ModelMapper mapper) {
        return mapper.map(team, QTeam.class);
    }
}
