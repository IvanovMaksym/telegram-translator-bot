package max.telegram.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotConfig {

    private String botUsername;
    private String botToken;
    private String yandexToken;
    private String yandexUrl;
    private static BotConfig botConfig = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(BotConfig.class);

    private BotConfig() {
        this.botUsername = read("bot.username");
        this.botToken = read("bot.token");
        this.yandexToken = read("yandex.token");
        this.yandexUrl = read("yandex.url");
    }

    public static BotConfig getInstance(){
        if (botConfig == null){
            botConfig = new BotConfig();
        }
        return botConfig;
    }


    public String getBotUsername(){
        return botUsername;
    }

    public String getBotToken(){
        return botToken;
    }

    public String getYandexToken(){
        return yandexToken;
    }

    public String getYandexUrl() {
        return yandexUrl;
    }

    private String read(String property) {
        Properties properties = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("botconfig.properties");
            if (resource != null) {
                properties.load(resource.openStream());
            }
            return String.valueOf(properties.get(property));
        } catch (IOException e) {
            LOGGER.error("Failed reading config properties file." + e);
            throw new RuntimeException();
        }
    }

}
