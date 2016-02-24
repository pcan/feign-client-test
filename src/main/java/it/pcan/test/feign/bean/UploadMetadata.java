package it.pcan.test.feign.bean;

import java.io.Serializable;

/**
 *
 * @author Pierantonio Cangianiello
 */
public class UploadMetadata implements Serializable {

    private String username;

    public UploadMetadata() {
    }

    public UploadMetadata(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
