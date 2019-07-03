package max.telegram.model;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Keyboard {

    private Keyboard() {
    }

    public static InlineKeyboardMarkup buildKeyboard(List<String> supportedLanguages, String commandId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Language language : Language.values()) {
            rowInline.add(new InlineKeyboardButton()
                .setCallbackData(commandId + "-" + language.getLanguageCode())
                .setText(language.getEmoji().getUnicode()
                    .concat(language.getLanguageName()
                    .concat(supportedLanguages.contains(language.getLanguageCode()) ? "\u2714" : ""))));
        }
        rowInline.forEach(x -> rowsInline.add(Collections.singletonList(x)));
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

}
