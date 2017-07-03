package max.telegram.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import max.telegram.commands.LanguagesCommand;
import max.telegram.commands.MyLanguageCommand;
import max.telegram.db.UserProfileDao;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.commands.BotCommand;

public class Keyboard {

    private final UserProfileDao userProfileDao = UserProfileDao.getInstance();
    private User user;

    public Keyboard(User user) {
        this.user = user;
    }

    public InlineKeyboardMarkup buildKeyboard(BotCommand command) {
        List<String> supportedLanguages = new ArrayList<>();
        if(command instanceof LanguagesCommand) {
            supportedLanguages = userProfileDao.retrieveLanguagesForUser(user).stream().map
                (UserToLanguage::getLanguage).collect(Collectors.toList());
        } else if (command instanceof MyLanguageCommand) {
            supportedLanguages = userProfileDao.getUserLanguage(user);
        }
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Language language : Language.values()) {
            rowInline.add(new InlineKeyboardButton()
                .setCallbackData(command.getCommandIdentifier() + "-" + language.getLanguageCode())
                .setText(language.getEmoji().getUnicode()
                    .concat(language.getLanguageName()
                    .concat(supportedLanguages.contains(language.getLanguageCode()) ? "\u2714" : ""))));
        }
        rowInline.forEach(x -> rowsInline.add(Collections.singletonList(x)));
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
}
