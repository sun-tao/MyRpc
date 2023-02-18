package github.rpc;

import java.io.Serializable;

public class Blog implements Serializable {
    private int id;
    private String userId;
    private String title;

    public Blog(int id, String userId, String title) {
        this.id = id;
        this.userId = userId;
        this.title = title;
    }

    public Blog() {

    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
