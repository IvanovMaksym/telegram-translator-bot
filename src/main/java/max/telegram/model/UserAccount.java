package max.telegram.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "USER")
public class UserAccount {

    public UserAccount() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer id;
    @Column(name = "TELEGRAM_ID")
    private Integer telegramId;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "USERNAME")
    private String userName;
    @Column(name = "NATIVE_LANGUAGE")
    private String nativeLanguage;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "USER_LANGUAGE", joinColumns = {@JoinColumn(name = "USER_ID")})
    @Column(name = "LANGUAGE", nullable = false)
    private Set<String> languageCodes = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Integer telegramId) {
        this.telegramId = telegramId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public void setNativeLanguage(String nativeLanguage) {
        this.nativeLanguage = nativeLanguage;
    }

    public Set<String> getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(Set<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(telegramId, that.telegramId) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(nativeLanguage, that.nativeLanguage) &&
                Objects.equals(languageCodes, that.languageCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, telegramId, firstName, lastName, userName, nativeLanguage, languageCodes);
    }
}
