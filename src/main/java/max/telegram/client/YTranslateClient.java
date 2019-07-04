package max.telegram.client;

import com.google.gson.Gson;
import max.telegram.model.YTranslatorResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.protocol.HTTP.USER_AGENT;

@Component
public class YTranslateClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(YTranslateClient.class);
    private final Gson GSON = new Gson();
    private final HttpClient httpClient = HttpClientBuilder.create().build();
    @Value("${yandex.url}")
    private String yandexUrl;
    @Value("${yandex.token}")
    private String yandexToken;

    public String translate(String text, String language) {
        LOGGER.info("Got a request to translate text '{}' in language {}", text, language);
        String result;
        try {
            HttpPost post = buildRequest(text, language);
            HttpResponse response = httpClient.execute(post);

            StringWriter sw = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), sw, "UTF-8");
            result = sw.toString();
        } catch (IOException e) {
            LOGGER.error("Error while translating text" + e);
            throw new RuntimeException();
        }

        YTranslatorResponse yTranslatorResponse = GSON.fromJson(result, YTranslatorResponse.class);
        String translatedText = yTranslatorResponse.getText().get(0);
        LOGGER.info("Returning translation: {}", translatedText);
        return translatedText;
    }

    public List<String> translateBulk(String text, List<String> languages) {
        return languages.stream().map(x -> translate(text, x)).collect(Collectors.toList());
    }

    private HttpPost buildRequest(String text, String lang) throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(yandexUrl);
        post.setHeader("UserProfile-Agent", USER_AGENT);
        post.setHeader("Accept", "*/*");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", yandexToken));
        urlParameters.add(new BasicNameValuePair("text", new String(text.getBytes(), StandardCharsets.UTF_8)));
        urlParameters.add(new BasicNameValuePair("lang", lang));
        post.setEntity(new UrlEncodedFormEntity(urlParameters, "utf-8"));

        return post;
    }
}
