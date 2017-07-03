package max.telegram.commands;

import max.telegram.db.UserProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class StartCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartCommand.class);
    private UserProfileDao userProfileDao = UserProfileDao.getInstance();

    public StartCommand() {
        super("start", "Set languages for translation");
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        userProfileDao.persistUser(user);
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.enableHtml(true);
        message.setText("Hey " + user.getFirstName() + " " + user.getLastName() + "! Go ahead and click /languages to "
            + "select the "
            + "languages "
            + "to "
            + "translate to! Try"
            + " inline mode in any chat. For the sake of demo select multiple ones, then you might go back and update "
            + "your choice.");

        try {
            sender.sendMessage(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending text message from StartCommand" + e);
        }
    }
}
