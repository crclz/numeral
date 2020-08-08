package fullforum.dto.out;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import fullforum.data.models.Article;
import lombok.Data;
import org.modelmapper.ModelMapper;

// '@Data' 是Lombok包的功能，Lombok需要IDEA额外安装Lombok扩展
// 使用@Data的目的是提供getter和setter，让ModelMapper正常工作。ModelMapper用于将数据库实体映射到DTO。
@Data
public class QArticle {
    public long id;
    public String title;
    public String text;
    public long userId;

    public Quser user;

    public static QArticle convert(Article p, Quser user, ModelMapper mapper) {
        var qArticle = mapper.map(p, QArticle.class);
        qArticle.user = user;
        return qArticle;
    }
}
