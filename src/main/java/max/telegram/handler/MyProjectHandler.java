package max.telegram.handler;

import com.google.cloud.speech.spi.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import max.telegram.client.YTranslateClient;
import max.telegram.commands.LanguagesCommand;
import max.telegram.commands.StartCommand;
import max.telegram.config.BotConfig;
import max.telegram.db.UserProfileDao;
import max.telegram.db.UserToLanguage;
import max.telegram.model.Keyboard;
import max.telegram.model.Language;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class MyProjectHandler extends TelegramLongPollingCommandBot {

    private ExecutorService executorService;
    private static BotConfig botConfig = BotConfig.getInstance();
    private static YTranslateClient client = new YTranslateClient();
    private UserProfileDao userProfileDao;

    public MyProjectHandler() {
        executorService = Executors.newFixedThreadPool(10);
        register(new LanguagesCommand());
        register(new StartCommand());
        userProfileDao = new UserProfileDao();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        executorService.execute(() -> {

            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            }
            if (update.hasInlineQuery()) {
                handleInlineQuery(update);
            }
            if (update.hasCallbackQuery()) {
                handleCallBachQuery(update);
            }
            if (update.hasMessage() && update.getMessage().getVoice() != null) {
                try {
                    handleVoice(update);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleVoice(Update update) throws Exception {
        String fileId = update.getMessage().getVoice().getFileId();

        String fileUrl = getFile(new GetFile().setFileId(fileId)).getFileUrl(BotConfig.getInstance().getBotToken());

        URL voiceURL = new URL(fileUrl);
        byte[] data = IOUtils.toByteArray(voiceURL.openStream());
        ByteString audioBytes = ByteString.copyFrom(data);

        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
            .setSampleRateHertz(16000)
            .setLanguageCode("en-US")
            .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setContent(audioBytes)
            .build();

        SpeechRecognitionAlternative alternativeToReturn;
        SendMessage sendMessageRequest;

        try (SpeechClient speech = SpeechClient.create()) {
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            Optional<List<SpeechRecognitionAlternative>> alternatives = results.stream().map
                (SpeechRecognitionResult::getAlternativesList).findFirst();
            if (alternatives.isPresent()) {
                alternativeToReturn = alternatives.get().get(0);
                sendMessageRequest = new SendMessage();
                sendMessageRequest.setText(alternativeToReturn.getTranscript());
                sendMessageRequest.setChatId(update.getMessage().getChatId());
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton("Ready to fly to some chat!")
                    .setSwitchInlineQuery(alternativeToReturn.getTranscript()));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessageRequest.setReplyMarkup(markupInline);

            } else {
                sendMessageRequest = new SendMessage();
                sendMessageRequest.setText("Sorry I didn't get that. Please try again ^^");
                sendMessageRequest.setChatId(update.getMessage().getChatId());
            }

        } catch (Exception e) {
            System.out.println("ERROR occurred in Google Speech Api ");
            throw e;
        }
        sendMessage(sendMessageRequest);

    }

    private void handleCallBachQuery(Update update) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = update.getCallbackQuery().getFrom();
        userProfileDao.persistLanguagesForUser(user, update
            .getCallbackQuery()
            .getData());
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        Keyboard keyboard = new Keyboard(user, userProfileDao);
        editMessageReplyMarkup.setReplyMarkup(keyboard.buildKeyboard())
            .setChatId(chatId)
            .setMessageId(messageId);
        try {
            editMessageReplyMarkup(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleInlineQuery(Update update) {
        InlineQuery inlineQuery = update.getInlineQuery();
        if (!inlineQuery.hasQuery()) {
            return;
        }
        List<Language> languages = userProfileDao.retrieveLanguagesForUser(update.getInlineQuery().getFrom())
            .stream().map(UserToLanguage::getLanguage)
            .map(Language::fromLanguageCode)
            .filter(Objects::nonNull)
            .collect
                (Collectors.toList());
        List<InlineQueryResult> resultArticles = new ArrayList<>();
        for (Language language : languages) {
            String translation = client.translate(inlineQuery.getQuery(), language.getLanguageCode());
            InlineQueryResultArticle resultArticle = buildInlineQueryResultArticle(translation, language);
            resultArticles.add(resultArticle);
        }
        AnswerInlineQuery answer = new AnswerInlineQuery();
        answer.setInlineQueryId(inlineQuery.getId());
        answer.setResults(resultArticles);
        try {
            answerInlineQuery(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Update update) {
        Message message = update.getMessage();
        try {
            String response = client.translate(new String(message.getText().getBytes()), "ru");
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

    private InlineQueryResultArticle buildInlineQueryResultArticle(String translation, Language language) {
        return new InlineQueryResultArticle()
            .setId(RandomStringUtils.random(7))
            .setTitle(language.getLanguageName())
            .setInputMessageContent(new InputTextMessageContent().setMessageText(translation))
            .setDescription(translation)
            .setThumbUrl(language.getLogoUrl());
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
