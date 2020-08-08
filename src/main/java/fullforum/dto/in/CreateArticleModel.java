package fullforum.dto.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateArticleModel {

    // 这些annotation用于请求模型验证
    @NotNull
    @NotBlank
    public String title;

    @NotNull
    @NotBlank
    public String text;

    // 必须要有空构造函数
    public CreateArticleModel() {
    }

    // 这个构造函数是为了方便测试的时候构造输入数据
    public CreateArticleModel(String title, String text) {
        this.title = title;
        this.text = text;
    }
}
