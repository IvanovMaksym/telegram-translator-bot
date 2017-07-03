package max.telegram.commands;

import max.telegram.db.UserProfileDao;
import max.telegram.model.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class MyLanguageCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyLanguageCommand.class);
    private final UserProfileDao userProfileDao = UserProfileDao.getInstance();

    public MyLanguageCommand() {
        super("mylanguage", "Native language for a user");
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        LOGGER.info("{} command is invoked", this.getCommandIdentifier());
        SendMessage sendMessage = new SendMessage(chat.getId(), "Please select your native language");
        Keyboard keyboard = new Keyboard(user);
        sendMessage.setReplyMarkup(keyboard.buildKeyboard(this));
        try {
            sender.sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending response to the command {}", this.getCommandIdentifier(), e);
        }
    }
}
