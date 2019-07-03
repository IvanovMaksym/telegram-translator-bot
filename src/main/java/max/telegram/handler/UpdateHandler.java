package max.telegram.handler;

import max.telegram.client.YTranslateClient;
import max.telegram.commands.LanguagesCommand;
import max.telegram.commands.MyLanguageCommand;
import max.telegram.commands.StartCommand;
import max.telegram.config.BotConfig;
import max.telegram.db.UserProfileRepository;
import max.telegram.model.Keyboard;
import max.telegram.model.Language;
import max.telegram.model.UserAccount;
import max.telegram.model.UserLanguage;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class UpdateHandler extends TelegramLongPollingCommandBot {

    private static final BotConfig botConfig = BotConfig.getInstance();
    private static final YTranslateClient client = new YTranslateClient();
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);
    private UserProfileRepository userProfileRepository;

    @Autowired
    public UpdateHandler(UserProfileRepository userProfileRepository,
                         LanguagesCommand languagesCommand,
                         MyLanguageCommand myLanguageCommand, StartCommand startCommand) {
        super(BotConfig.getInstance().getBotUsername());
        this.userProfileRepository = userProfileRepository;
        register(languagesCommand);
        register(startCommand);
        register(myLanguageCommand);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        System.out.println("processNonCommandUpdate is invoked");if (update.hasMessage() && update.getMessage().hasText()) {
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
//        LOGGER.info("Got a voice message. About to download from telegram servers.");
//        String fileId = update.getMessage().getVoice().getFileId();
//
//        String fileUrl = getFile(new GetFile().setFileId(fileId)).getFileUrl(BotConfig.getInstance().getBotToken());
//
//        URL voiceURL = new URL(fileUrl);
//        byte[] data = IOUtils.toByteArray(voiceURL.openStream());
//        ByteString audioBytes = ByteString.copyFrom(data);
//        LOGGER.info("Audio downloaded from Telegram servers");
//
//        RecognitionConfig config = RecognitionConfig.newBuilder()
//            .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
//            .setSampleRateHertz(16000)
//            .setLanguageCode(userProfileDao.getUserLanguage(update.getMessage().getFrom()).get(0))
//            .build();
//        RecognitionAudio audio = RecognitionAudio.newBuilder()
//            .setContent(audioBytes)
//            .build();
//
//        SpeechRecognitionAlternative alternativeToReturn;
//        SendMessage sendMessageRequest;
//
//        try (SpeechClient speech = SpeechClient.create()) {
//            LOGGER.info("About to speech recognize the voice message...");
//            RecognizeResponse response = speech.recognize(config, audio);
//            List<SpeechRecognitionResult> results = response.getResultsList();
//
//            Optional<List<SpeechRecognitionAlternative>> alternatives = results.stream().map
//                (SpeechRecognitionResult::getAlternativesList).findFirst();
//            if (alternatives.isPresent()) {
//                alternativeToReturn = alternatives.get().get(0);
//                LOGGER.info("Speech recognized: {}, sending back to chat", alternativeToReturn);
//                sendMessageRequest = new SendMessage();
//                sendMessageRequest.setText("You said: " + alternativeToReturn.getTranscript());
//                sendMessageRequest.setChatId(update.getMessage().getChatId());
//                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
//                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
//                List<InlineKeyboardButton> rowInline = new ArrayList<>();
//                rowInline.add(new InlineKeyboardButton("Click here and select any chat!")
//                    .setSwitchInlineQuery(alternativeToReturn.getTranscript()));
//                rowsInline.add(rowInline);
//                markupInline.setKeyboard(rowsInline);
//                sendMessageRequest.setReplyMarkup(markupInline);
//
//            } else {
//                LOGGER.info("Speech could not be recognized. Sending message to chat");
//                sendMessageRequest = new SendMessage();
//                sendMessageRequest.setText("Sorry I didn't get that. Please try again ^^");
//                sendMessageRequest.setChatId(update.getMessage().getChatId());
//            }
//
//        } catch (Exception e) {
//            LOGGER.error("ERROR occurred in Google Speech Api " + e);
//            throw new RuntimeException();
//        }
//        try {
//            sendMessage(sendMessageRequest);
//        } catch (TelegramApiException e) {
//            LOGGER.error("Error while sending message!" + e);
//        }
    }

    private void handleCallBackQuery(Update update) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = update.getCallbackQuery().getFrom();
        String callBackQueryData = update.getCallbackQuery().getData();
        String fromCommand = callBackQueryData.split("-")[0];
        String language = callBackQueryData.split("-")[1];
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        try {
            UserAccount userAccount = userProfileRepository.findByTelegramId(user.getId()).orElse(new UserAccount());//TODO handle properly
            if ("mylanguage".equals(fromCommand)) {
                UserLanguage userLanguage = new UserLanguage();
                userLanguage.setNative(true);
                userAccount.getLanguageCodes().add(userLanguage);
                userProfileRepository.save(userAccount);
            } else if ("languages".equals(fromCommand)) {
                List<UserLanguage> languageCodes = userAccount.getLanguageCodes();
                List<UserLanguage> matchedLanguage = languageCodes.stream()
                        .filter(userLanguage -> userLanguage.getLanguageCode().equals(language))
                        .collect(Collectors.toList());
                if (matchedLanguage.isEmpty()) {
                    UserLanguage userLanguage = new UserLanguage();
                    userLanguage.setLanguageCode(language);
                    languageCodes.add(userLanguage);
                } else {
                    languageCodes.remove(matchedLanguage.get(0));
                }
                userProfileRepository.save(userAccount);
            }
            editMessageReplyMarkup.setReplyMarkup(Keyboard.buildKeyboard(userAccount.getLanguageCodes()
                    .stream()
                    .map(UserLanguage::getLanguageCode)
                    .collect(Collectors.toList()), getRegisteredCommand(fromCommand).getCommandIdentifier()))
                    .setChatId(chatId)
                    .setMessageId(messageId);
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            LOGGER.error("Error while handling CallBackQuery", e);
        }


    }

    private void handleInlineQuery(Update update) {
        InlineQuery inlineQuery = update.getInlineQuery();
        if (!inlineQuery.hasQuery()) {
            return;
        }
        List<Language> languages = userProfileRepository.findByTelegramId(update.getInlineQuery().getFrom().getId())
                .orElseThrow(RuntimeException::new)
                .getLanguageCodes()
                .stream().map(UserLanguage::getLanguageCode)
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
            execute(answer);
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

            execute(sendMessageRequest);
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
    public String getBotToken() {
        return botConfig.getBotToken();
    }
}
