package max.telegram.commands;

import max.telegram.db.UserProfileRepository;
import max.telegram.model.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class StartCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartCommand.class);
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public StartCommand(UserProfileRepository userProfileRepository) {
        super("start", "Set languages for translation");
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        UserAccount userAccount = new UserAccount();
        userAccount.setFirstName(user.getFirstName());
        userAccount.setLastName(user.getLastName());
        userAccount.setTelegramId(user.getId());
        userAccount.setNativeLanguage(user.getLanguageCode());

        userProfileRepository.save(userAccount);

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
            sender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending text message from StartCommand" + e);
        }
    }
}
