package it.pcan.test.feign.bean;

import java.io.Serializable;

/**
 *
 * @author Pierantonio Cangianiello
 */
public class UploadInfo implements Serializable {

    private int id;
    private long size;
    private String name;

    public UploadInfo() {
    }

    public UploadInfo(int id, long size, String name) {
        this.id = id;
        this.size = size;
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UploadInfo{" + "id=" + id + ", size=" + size + ", name=" + name + '}';
    }

}
