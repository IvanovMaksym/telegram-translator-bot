package max.telegram.handler;

import max.telegram.client.YTranslateClient;
import max.telegram.config.BotConfig;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyProjectHandler extends TelegramLongPollingBot {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private BotConfig botConfig = BotConfig.getInstance();

    public void onUpdateReceived(final Update update) {
        executorService.execute(() -> {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            }
            if (update.hasInlineQuery()) {
                try {
                    handleInlineQuery(update);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
}

    private void handleInlineQuery(Update update) throws IOException {
        YTranslateClient client = new YTranslateClient();

        InlineQuery inlineQuery = update.getInlineQuery();
        if (inlineQuery.hasQuery()) {
            String translation = client.translate(inlineQuery.getQuery());

            InputTextMessageContent inputTextMessageContent = new InputTextMessageContent();
            inputTextMessageContent.setMessageText(translation);

            InlineQueryResultArticle result = new InlineQueryResultArticle();
            result.setId(inlineQuery.getId());
            result.setTitle("Russian translation:");
            result.setInputMessageContent(inputTextMessageContent);
            result.setDescription(translation);

            AnswerInlineQuery answer = new AnswerInlineQuery();
            answer.setInlineQueryId(inlineQuery.getId());
            answer.setResults(Arrays.asList(result));
            try {
                answerInlineQuery(answer);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleTextMessage(Update update) {
        Message message = update.getMessage();
        YTranslateClient client = new YTranslateClient();
        try {
            String response = client.translate(new String(message.getText().getBytes(), "UTF-8"));
            SendMessage sendMessageRequest = new SendMessage();
            sendMessageRequest.setText(new String(response.getBytes(), "UTF-8"));
            sendMessageRequest.setChatId(message.getChatId().toString());
            sendMessage(sendMessageRequest);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
