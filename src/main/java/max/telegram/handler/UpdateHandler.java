package max.telegram.handler;

import max.telegram.client.YTranslateClient;
import max.telegram.commands.LanguagesCommand;
import max.telegram.commands.StartCommand;
import max.telegram.db.UserProfileRepository;
import max.telegram.model.Keyboard;
import max.telegram.model.Language;
import max.telegram.model.UserAccount;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class UpdateHandler extends TelegramLongPollingCommandBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);
    private final YTranslateClient yandexClient;
    private final UserProfileRepository userProfileRepository;
    private final String botToken;

    @Autowired
    public UpdateHandler(UserProfileRepository userProfileRepository, YTranslateClient yandexClient,
                         LanguagesCommand languagesCommand, StartCommand startCommand,
                         @Value("${bot.token}") String botToken, @Value("${bot.username}") String botUsername) {
        super(botUsername);
        this.userProfileRepository = userProfileRepository;
        this.yandexClient = yandexClient;
        this.botToken = botToken;
        register(languagesCommand);
        register(startCommand);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        System.out.println("processNonCommandUpdate is invoked");
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasInlineQuery()) {
            handleInlineQuery(update);
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update);
        } else throw new UnsupportedOperationException("The operation is not supported yet! ");
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
                userAccount.setNativeLanguage(language);
                userProfileRepository.save(userAccount);
            } else if ("languages".equals(fromCommand)) {
                List<String> languageCodes = userAccount.getLanguageCodes();
                List<String> matchedLanguage = languageCodes.stream()
                        .filter(userLanguage -> userLanguage.equals(language))
                        .collect(Collectors.toList());
                if (matchedLanguage.isEmpty()) {
                    languageCodes.add(language);
                } else {
                    languageCodes.remove(matchedLanguage.get(0));
                }
                userProfileRepository.save(userAccount);
            }
            editMessageReplyMarkup.setReplyMarkup(
                    Keyboard.buildKeyboard(userAccount.getLanguageCodes(), getRegisteredCommand(fromCommand).getCommandIdentifier()))
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
                .stream()
                .map(Language::fromLanguageCode)
                .filter(Objects::nonNull)
                .collect
                        (Collectors.toList());
        List<InlineQueryResult> resultArticles = new ArrayList<>();
        for (Language language : languages) {
            String translation = yandexClient.translate(inlineQuery.getQuery(), language.getLanguageCode().split("_")[0]);
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
            String response = yandexClient.translate(new String(message.getText().getBytes()), "ru");
            SendMessage sendMessageRequest = new SendMessage();
            sendMessageRequest.setText(new String(response.getBytes(), StandardCharsets.UTF_8));
            sendMessageRequest.setChatId(message.getChatId().toString());

            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
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
        return botToken;
    }
}
