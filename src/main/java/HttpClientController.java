import com.fasterxml.jackson.core.JsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientController {
    private String RESOURCE_POINT;
    HttpClient client = null;
    HttpClientController() {
        client = HttpClient.newBuilder().build();
    }

}

class ChatBot extends HttpClientController {
    String userid = "***REMOVED***";
    String RESOURCE_POINT = "https://aiproject.ru/api/";

    ChatBot() {
        super();
    }

    HttpRequest requestAnswer(String msg) {
        String ask = "\"ask\":\"" + msg + "\",";
        String userid = "\"userid\":\"" + this.userid + "\",";
        String key = "\"key\":\"\"";
        String query = "{" + ask + userid + key + "}";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(RESOURCE_POINT))
                .POST(HttpRequest.BodyPublishers.ofString("query=" + query))
                .build();

        return httpRequest;
    }

    public String getAnswer(String msg) {
        HttpRequest request = requestAnswer(msg);
        String answer = "Что-то пошло не так";
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body().toString()).getAsJsonObject();
            int status = jsonResponse.get("status").getAsInt();
            if (status == 1) {
                answer = jsonResponse.get("aiml").getAsString();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return answer;
    }
}
