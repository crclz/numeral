package fullforum.dto.in;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class PatchArticleModel {
    @NotBlank
    public String title;

    @NotBlank
    public String text;

    public PatchArticleModel() {
    }

    public PatchArticleModel(String title, String text) {
        this.title = title;
        this.text = text;
    }
}
