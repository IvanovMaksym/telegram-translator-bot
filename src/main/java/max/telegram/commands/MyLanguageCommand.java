package max.telegram.commands;

import max.telegram.db.UserProfileRepository;
import max.telegram.model.Keyboard;
import max.telegram.model.UserLanguage;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyLanguageCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyLanguageCommand.class);
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public MyLanguageCommand(UserProfileRepository userProfileRepository) {
        super("mylanguage", "Native language for a user");
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        LOGGER.info("{} command is invoked", this.getCommandIdentifier());
        SendMessage sendMessage = new SendMessage(chat.getId(), "Please select your native language");
        List<String> supportedLanguages = userProfileRepository.findByTelegramId(user.getId()).orElseThrow(RuntimeException::new)
                .getLanguageCodes().stream()
                .map(UserLanguage::getLanguageCode)
                .collect(Collectors.toList());

        InlineKeyboardMarkup markupInline = Keyboard.buildKeyboard(supportedLanguages, getCommandIdentifier());
        sendMessage.setReplyMarkup(markupInline);
        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending response to the command {}", this.getCommandIdentifier(), e);
        }
    }
}
