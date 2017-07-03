package max.telegram.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "USER_ID_LANGUAGE", uniqueConstraints = {
    @UniqueConstraint(columnNames = "RECORD_ID")})
public class UserToLanguage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECORD_ID", unique = true, nullable = false)
    private Integer recordId;

    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "ACTIVE")
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
