package max.telegram.db;

import java.io.Serializable;

public class UserToLanguage implements Serializable {


    private Integer recordId;
    private Integer userId;
    private String language;
    private int active;

    public UserToLanguage() {
    }

    public UserToLanguage(Integer userId, String language) {
        this.userId = userId;
        this.language = language;
        this.setActive(1);
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
