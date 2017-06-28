package max.telegram.model;

public enum Language {

    EN(
        "en",
        "English",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Union_flag_1606_%28Kings_Colors%29.svg/2000px-Union_flag_1606_%28Kings_Colors%29.svg.png"
    ),
    ES(
        "es",
        "Spanish",
        "http://orig13.deviantart.net/760d/f/2015/344/c/4/republic_of_spain___flag__1820_present__by_kike_92-d9jntu8.png"
    ),
    RU(
        "ru",
        "Russian",
        "https://static.webshopapp.com/shops/094414/files/057928038/russia-flag-emoji-free-download.jpg"),
    DE(
        "de",
        "German",
        "http://www.planwallpaper.com/static/images/German-Flag.jpg"),
    PL(
        "pl",
        "Polish",
        "http://www.itouchapps.net/images/flag-play-fun-with-flags-quiz/poland1.jpg"),
    CZ(
        "cs",
        "Czech",
        "https://www.pegasus-europe.org/userfiles/images/cz.gif"
    );

    private String languageCode;
    private String languageName;
    private String logoUrl;

    public static Language fromLanguageCode(String languageCode) {
        for (Language language : Language.values()) {
            if (language.getLanguageCode().equals(languageCode)) {
                return language;
            }
        }
        return null;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    Language(String languageCode, String languageName,  String logoUrl) {
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.logoUrl = logoUrl;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }
}
