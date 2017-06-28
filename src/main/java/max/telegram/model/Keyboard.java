package max.telegram.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import max.telegram.db.UserProfileDao;
import max.telegram.db.UserToLanguage;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class Keyboard {

    private UserProfileDao userProfileDao;
    private User user;

    public Keyboard(User user, UserProfileDao userProfileDao) {
        this.user = user;
        this.userProfileDao = userProfileDao;
    }

    public InlineKeyboardMarkup buildKeyboard() {
        List<String> supportedLanguages = userProfileDao.retrieveLanguagesForUser(user).stream().map
            (UserToLanguage::getLanguage).collect(Collectors.toList());
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Language language : Language.values()) {
            if (supportedLanguages.contains(language.getLanguageCode())) {
                rowInline.add(new InlineKeyboardButton().setText(language.getLanguageName() + " " + "\u2714")
                    .setCallbackData
                    (language.getLanguageCode()));
            } else {
                rowInline.add(new InlineKeyboardButton().setText(language.getLanguageName()).setCallbackData(language
                    .getLanguageCode()));
            }
        }

        rowInline.forEach(x -> rowsInline.add(Collections.singletonList(x)));
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }


}
