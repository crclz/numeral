package fullforum.dto.out;

/**
 * 这个类用于返回id
 * 很多时候，用户发起一个请求，创建了一个实体（例如发表文章），服务器最好将这个新的实体的id返回
 */
public class IdDto {
    public long id;

    public IdDto(long id) {
        this.id = id;
    }
}
