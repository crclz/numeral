package fullforum.data.models;

import fullforum.data.RootEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Document extends RootEntity {

    @Column(columnDefinition = "text", length = 65535)
    private String data;

    protected Document() {
        // Required by jpa
    }

    public Document(long id) {
        super(id);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
