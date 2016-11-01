package max.telegram.model;

import java.util.List;


public class YTranslatorResponse {

    private int code;
    private String lang;
    private List<String> text;

    public int getCode() {
        return code;
    }

    public String getLang() {
        return lang;
    }

    public List<String> getText() {
        return text;
    }
}
