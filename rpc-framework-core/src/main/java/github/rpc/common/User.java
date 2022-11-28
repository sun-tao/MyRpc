package github.rpc.common;
import java.io.Serializable;

public class User implements Serializable {
    private Integer id;
    private String username;
    private Boolean sex;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", sex=" + sex +
                '}';
    }

    public User(){

    }

    public User(Integer id, String username, Boolean sex) {
        this.id = id;
        this.username = username;
        this.sex = sex;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }
}
