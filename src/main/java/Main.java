import max.telegram.handler.MyProjectHandler;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * Created by SG0221984 on 10/27/2016.
 */
public class Main {

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new MyProjectHandler());
        } catch (TelegramApiException e) {
            BotLogger.error("", e);
        }
    }
}
