package it.pcan.test.feign.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Pierantonio Cangianiello
 */
public class UploadMetadata implements Serializable {

    private String username;

    private Integer position;

    public UploadMetadata() {
    }

    public UploadMetadata(String username, Integer position) {
        this.username = username;
        this.position = position;
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

    @Override
    public String toString() {
        return "UploadMetadata{" + "username=" + username + ", position=" + position + '}';
    }

}
