package fullforum.data.models;

import fullforum.data.RootEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Blob2 extends RootEntity {
    @Column(length = 1024 * 1024 * 20)// 20M
    private byte[] data;

    protected Blob2() {
    }

    public Blob2(long id, byte[] data) {
        super(id);
        setData(data);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
