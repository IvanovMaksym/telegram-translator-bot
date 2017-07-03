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
import max.telegram.commands.MyLanguageCommand;
import max.telegram.commands.StartCommand;
import max.telegram.config.BotConfig;
import max.telegram.db.UserProfileDao;
import max.telegram.model.UserToLanguage;
import max.telegram.model.Keyboard;
import max.telegram.model.Language;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class UpdateHandler extends TelegramLongPollingCommandBot {

    private static final BotConfig botConfig = BotConfig.getInstance();
    private static final YTranslateClient client = new YTranslateClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);
    private UserProfileDao userProfileDao = UserProfileDao.getInstance();
    private ExecutorService executorService;

    public UpdateHandler() {
        executorService = Executors.newFixedThreadPool(10);
        register(new LanguagesCommand());
        register(new StartCommand());
        register(new MyLanguageCommand());
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        executorService.execute(() -> handle(update));
    }

    private void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasInlineQuery()) {
            handleInlineQuery(update);
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update);
        } else if (update.hasMessage() && update.getMessage().getVoice() != null) {
            try {
                handleVoice(update);
            } catch (Exception e) {
                LOGGER.error("Error while processing voice message!" + e);
            }
        } else throw new UnsupportedOperationException("The operation is not supported yet! ");
    }

    private void handleVoice(Update update) throws Exception {
        LOGGER.info("Got a voice message. About to download from telegram servers.");
        String fileId = update.getMessage().getVoice().getFileId();

        String fileUrl = getFile(new GetFile().setFileId(fileId)).getFileUrl(BotConfig.getInstance().getBotToken());

        URL voiceURL = new URL(fileUrl);
        byte[] data = IOUtils.toByteArray(voiceURL.openStream());
        ByteString audioBytes = ByteString.copyFrom(data);
        LOGGER.info("Audio downloaded from Telegram servers");

        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
            .setSampleRateHertz(16000)
            .setLanguageCode(userProfileDao.getUserLanguage(update.getMessage().getFrom()).get(0))
            .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setContent(audioBytes)
            .build();

        SpeechRecognitionAlternative alternativeToReturn;
        SendMessage sendMessageRequest;

        try (SpeechClient speech = SpeechClient.create()) {
            LOGGER.info("About to speech recognize the voice message...");
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            Optional<List<SpeechRecognitionAlternative>> alternatives = results.stream().map
                (SpeechRecognitionResult::getAlternativesList).findFirst();
            if (alternatives.isPresent()) {
                alternativeToReturn = alternatives.get().get(0);
                LOGGER.info("Speech recognized: {}, sending back to chat", alternativeToReturn);
                sendMessageRequest = new SendMessage();
                sendMessageRequest.setText("You said: " + alternativeToReturn.getTranscript());
                sendMessageRequest.setChatId(update.getMessage().getChatId());
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton("Click here and select any chat!")
                    .setSwitchInlineQuery(alternativeToReturn.getTranscript()));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessageRequest.setReplyMarkup(markupInline);

            } else {
                LOGGER.info("Speech could not be recognized. Sending message to chat");
                sendMessageRequest = new SendMessage();
                sendMessageRequest.setText("Sorry I didn't get that. Please try again ^^");
                sendMessageRequest.setChatId(update.getMessage().getChatId());
            }

        } catch (Exception e) {
            LOGGER.error("ERROR occurred in Google Speech Api " + e);
            throw new RuntimeException();
        }
        try {
            sendMessage(sendMessageRequest);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while sending message!" + e);
        }
    }

    private void handleCallBackQuery(Update update) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = update.getCallbackQuery().getFrom();
        String callBackQueryData = update.getCallbackQuery().getData();
        String fromCommand = callBackQueryData.split("-")[0];
        String language = callBackQueryData.split("-")[1];
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        Keyboard keyboard = new Keyboard(user);
        try {
            if ("mylanguage".equals(fromCommand)) {
                userProfileDao.updateUserNativeLanguage(user, language);
            } else if ("languages".equals(fromCommand)){
                userProfileDao.updateLanguagesForUser(user, language);
            }
            editMessageReplyMarkup.setReplyMarkup(keyboard.buildKeyboard(getRegisteredCommand(fromCommand)))
                .setChatId(chatId)
                .setMessageId(messageId);
            editMessageReplyMarkup(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while handling CallBackQuery", e);
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
            String translation = client.translate(inlineQuery.getQuery(), language.getLanguageCode().split("_")[0]);
            InlineQueryResultArticle resultArticle = buildInlineQueryResultArticle(translation, language);
            resultArticles.add(resultArticle);
        }

        AnswerInlineQuery answer = new AnswerInlineQuery();
        answer.setInlineQueryId(inlineQuery.getId());
        answer.setResults(resultArticles);
        try {
            answerInlineQuery(answer);
        } catch (TelegramApiException e) {
            LOGGER.error("Error answering inlineQuery. " + e);
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
        } catch (IOException | TelegramApiException e) {
            LOGGER.error("Error while handling text message!" + e);
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
