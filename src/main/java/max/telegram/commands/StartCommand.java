package max.telegram.commands;

import max.telegram.db.UserProfileDao;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class StartCommand extends BotCommand {

    private UserProfileDao userProfileDao;

    public StartCommand() {
        super("start", "Set languages for translation");
        userProfileDao = new UserProfileDao();
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        userProfileDao.persistUser(user);
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.enableHtml(true);
        message.setText("Hey There! Go ahead and click /languages to select the languages to translate to! Try"
            + " inline mode in any chat. For the sake of demo select multiple ones, then you might go back and update "
            + "your choice.");

        try {
            sender.sendMessage(message);
        } catch (TelegramApiException e) {
            System.out.println("error oops");
        }
    }
}
