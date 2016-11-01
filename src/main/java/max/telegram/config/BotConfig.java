package max.telegram.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public class BotConfig {

    private String botUsername;
    private String botToken;
    private String yandexToken;
    private static BotConfig botConfig = null;

    private BotConfig() {
        this.botUsername = read("bot.username");
        this.botToken = read("bot.token");
        this.yandexToken = read("yandex.token");
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
            e.printStackTrace();
            return "";
        }
    }

}
