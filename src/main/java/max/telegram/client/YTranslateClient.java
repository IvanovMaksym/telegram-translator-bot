package max.telegram.client;

import com.google.gson.Gson;
import max.telegram.config.BotConfig;
import max.telegram.model.YTranslatorResponse;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;


public class YTranslateClient {

    private HttpClient httpClient;
    private static final String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static BotConfig botConfig = BotConfig.getInstance();

    public YTranslateClient() {
        httpClient = HttpClientBuilder.create().build();
    }

    public String translate(String text) throws IOException {
        Gson gson = new Gson();
        HttpPost post = new HttpPost(url);

        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept", "*/*");

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", botConfig.getYandexToken()));
        urlParameters.add(new BasicNameValuePair("text", new String(text.getBytes(), "UTF-8")));
        urlParameters.add(new BasicNameValuePair("lang", "ru"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters, "utf-8"));

        HttpResponse response = httpClient.execute(post);
        System.out.println(Thread.currentThread().getName() + " Started.");
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        System.out.println("RQ: " + text );
        YTranslatorResponse yTranslatorResponse = gson.fromJson(result.toString(), YTranslatorResponse.class);
        String translatedText = yTranslatorResponse.getText().get(0);
        System.out.println("RS: " + translatedText);
        return translatedText;
    }


}
