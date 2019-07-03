package max.telegram.model;

import java.io.Serializable;
import java.util.Objects;


public class UserLanguageId implements Serializable {

    public UserLanguageId() {
    }

    private Long user;
    private String languageCode;

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLanguageId that = (UserLanguageId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, languageCode);
    }
}
