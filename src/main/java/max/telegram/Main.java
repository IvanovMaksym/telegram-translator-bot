package max.telegram;

import max.telegram.handler.UpdateHandler;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;


public class Main {

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            ApiContextInitializer.init();
            telegramBotsApi.registerBot(new UpdateHandler());
        } catch (TelegramApiException e) {
            BotLogger.error("", e);
        }
    }
}
