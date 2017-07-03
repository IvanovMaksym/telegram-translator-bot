package max.telegram.model;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

public enum Language {

    EN(
        "en_US",
        "English",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Union_flag_1606_%28Kings_Colors%29.svg/2000px"
            + "-Union_flag_1606_%28Kings_Colors%29.svg.png",
        EmojiManager.getForAlias("us")
    ),
    ES(
        "es_ES",
        "Spanish",
        "http://orig13.deviantart.net/760d/f/2015/344/c/4/republic_of_spain___flag__1820_present__by_kike_92"
            + "-d9jntu8.png",
        EmojiManager.getForAlias("es")
    ),
    RU(
        "ru_RU",
        "Russian",
        "https://static.webshopapp.com/shops/094414/files/057928038/russia-flag-emoji-free-download.jpg",
        EmojiManager.getForAlias("ru")),
    DE(
        "de_DE",
        "German",
        "http://www.planwallpaper.com/static/images/German-Flag.jpg",
        EmojiManager.getForAlias("de")),
    PL(
        "pl_PL",
        "Polish",
        "http://www.itouchapps.net/images/flag-play-fun-with-flags-quiz/poland1.jpg",
        EmojiManager.getForAlias("pl")),
    CZ(
        "cs_CZ",
        "Czech",
        "https://www.pegasus-europe.org/userfiles/images/cz.gif",
        EmojiManager.getForAlias("cz")
    ),
    FR(
        "fr_FR",
        "French",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Flag_of_France.svg/240px-Flag_of_France.svg.png",
        EmojiManager.getForAlias("fr")
    ),
    JA(
        "ja_JP",
        "Japanese",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Flag_of_Japan.svg/240px-Flag_of_Japan.svg.png",
        EmojiManager.getForAlias("japanese_ogre")
    );

    private String languageCode;
    private String languageName;
    private String logoUrl;
    private Emoji emoji;

    public static Language fromLanguageCode(String languageCode) {
        for (Language language : Language.values()) {
            if (language.getLanguageCode().equals(languageCode)) {
                return language;
            }
        }
        return null;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public void setEmoji(Emoji emoji) {
        this.emoji = emoji;
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

    Language(String languageCode, String languageName,  String logoUrl, Emoji emoji) {
        this.languageCode = languageCode;
        this.languageName = languageName;
        this.logoUrl = logoUrl;
        this.emoji = emoji;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }
}
