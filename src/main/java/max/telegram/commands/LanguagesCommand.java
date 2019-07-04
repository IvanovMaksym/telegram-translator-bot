package max.telegram.commands;

import max.telegram.db.UserProfileRepository;
import max.telegram.model.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Set;

@Component
public class LanguagesCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguagesCommand.class);

    private UserProfileRepository userProfileRepository;

    @Autowired
    public LanguagesCommand(UserProfileRepository userProfileRepository) {
        super("languages", "Set languages for translation");
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        LOGGER.info("Received command {}", this.getCommandIdentifier());
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.enableHtml(true);
        message.setText("Hey There! Here's a list with languages. Select the ones you want me to translate to!");
        Set<String> supportedLanguages = userProfileRepository.findByTelegramId(user.getId()).orElseThrow(RuntimeException::new)
                .getLanguageCodes();

        InlineKeyboardMarkup markupInline = Keyboard.buildKeyboard(supportedLanguages, getCommandIdentifier());
        message.setReplyMarkup(markupInline);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending text message from LanguagesCommand " + e);
        }
    }
}
