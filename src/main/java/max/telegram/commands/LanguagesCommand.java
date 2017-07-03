package max.telegram.commands;

import max.telegram.model.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class LanguagesCommand extends BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguagesCommand.class);

    public LanguagesCommand() {
        super("languages", "Set languages for translation");
    }

    @Override
    public void execute(AbsSender sender, User user, Chat chat, String[] strings) {
        LOGGER.info("Received command {}", this.getCommandIdentifier());
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message.enableHtml(true);
        message.setText("Hey There! Here's a list with languages. Select the ones you want me to translate to!");
        Keyboard keyboard = new Keyboard(user);
        InlineKeyboardMarkup markupInline = keyboard.buildKeyboard(this);
        message.setReplyMarkup(markupInline);

        try {
            sender.sendMessage(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending text message from LanguagesCommand " + e);
        }
    }
}
