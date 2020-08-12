package fullforum.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateArticleModel {

    // 这些annotation用于请求模型验证
    @NotNull
    @NotBlank
    public String title;

    @NotNull
    @NotBlank
    public String text;
}
