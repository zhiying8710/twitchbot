package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import constants.Config;
import okhttp3.*;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmotesGetter {
    private final String BTTV_RESOURCE_POINT = "https://api.betterttv.net";
    private final String FFZ_RESOURCE_POINT = "https://api.frankerfacez.com";
    private final String TWITCH_RESOURCE_POINT = "";

    OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(new File("http_cache"), 50L * 1024L * 1024L))
            .build();

    public EmotesGetter() {

    }

    public Map<String, String> getGlobalBTTVEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();
        String resource = BTTV_RESOURCE_POINT + "/3/cached/emotes/global";
        String urlTemplate = "//cdn.betterttv.net/emote/{{id}}/{{image}}";
        String image = "3x";

        Request request = new Request.Builder()
                .url(resource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .build();
        Response response = client.newCall(request).execute();

        JsonArray emotesArray = JsonParser.parseString(response.body().string()).getAsJsonArray();

        urlTemplate = urlTemplate.replace("//", "https://");
        for (JsonElement elem : emotesArray
        ) {
            JsonObject objElem = elem.getAsJsonObject();
            String id = objElem.get("id").getAsString();
            String code = objElem.get("code").getAsString();
            String url = urlTemplate.replace("{{id}}", id).replace("{{image}}", image);
            emotesMap.put(code, url);
        }

        response.close();

        return emotesMap;
    }

    public Map<String, String> getBTTVEmotes(String nick){
        Map<String, String> emotesMap = new HashMap<>();

        String resource = BTTV_RESOURCE_POINT + "/2/channels/" + nick;
        Request request = new Request.Builder()
                .url(resource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            JsonObject JSONresponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            String urlTemplate = JSONresponse.get("urlTemplate").getAsString();

            urlTemplate = urlTemplate.replace("//", "https://");
            JsonArray emotesArray = JSONresponse.getAsJsonArray("emotes");
            String image = "3x";
            for (JsonElement elem : emotesArray
            ) {
                JsonObject objElem = elem.getAsJsonObject();
                String id = objElem.get("id").getAsString();
                String code = objElem.get("code").getAsString();
                String url = urlTemplate.replace("{{id}}", id).replace("{{image}}", image);
                emotesMap.put(code, url);
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("[ERROR]Не удалось получить BTTV-смайлы");
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (final Throwable th) {
                    System.out.println(th.getMessage());
                }
            }
        }



        return emotesMap;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new EmotesGetter().getGlobalFFZEmotes();
    }
    public Map<String, String> getGlobalFFZEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();
        String globalEmotesResource = FFZ_RESOURCE_POINT + "/v1/set/3";

        Request request = new Request.Builder()
                .url(globalEmotesResource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .build();
        Response globalResponse = client.newCall(request).execute();

        JsonArray globalEmotes = JsonParser
                .parseString(globalResponse.body().string())
                .getAsJsonObject()
                .getAsJsonObject("set")
                .getAsJsonArray("emoticons");

        globalResponse.close();

        for (var elem : globalEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String name = emoteInfo.get("name").getAsString();
            JsonObject urls = emoteInfo.getAsJsonObject("urls");
            String url = "";
//            if (!urls.get("4").isJsonNull()) {
//                url = urls.get("4").getAsString();
//            } else if (!urls.get("2").isJsonNull()) {
//                url = urls.get("2").getAsString();
//            } else {
//                url = urls.get("1").getAsString();
//            }

            if (urls.has("4")) {
                url = urls.get("4").getAsString();
            } else if (urls.has("2")) {
                url = urls.get("2").getAsString();
            } else {
                url = urls.get("1").getAsString();
            }

            url = url.replaceFirst("//", "https://");

            emotesMap.put(name, url);
        }

        return emotesMap;
    }

    public Map<String, String> getFFZEmotes(String nick){
        Map<String, String> emotesMap = new HashMap<>();


        String channelEmotesResource = FFZ_RESOURCE_POINT + "/v1/room/" + nick;


        Request request = new Request.Builder()
                .url(channelEmotesResource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .build();
        Response channelResponse = null;
        try {
            channelResponse = client.newCall(request).execute();



        JsonObject channelInfoJson = JsonParser
                .parseString(channelResponse.body().string())
                .getAsJsonObject();

        String channelSetId = channelInfoJson
                .getAsJsonObject("room")
                .get("set")
                .getAsString();

        JsonArray channelEmotes = channelInfoJson
                .getAsJsonObject("sets")
                .getAsJsonObject(channelSetId)
                .getAsJsonArray("emoticons");


        for (var elem : channelEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String name = emoteInfo.get("name").getAsString();
            JsonObject urls = emoteInfo.getAsJsonObject("urls");
            String url = "";
//            if (!urls.get("4").isJsonNull()) {
//                url = urls.get("4").getAsString();
//            } else if (!urls.get("2").isJsonNull()) {
//                url = urls.get("2").getAsString();
//            } else {
//                url = urls.get("1").getAsString();
//            }

            if (urls.has("4")) {
                url = urls.get("4").getAsString();
            } else if (urls.has("2")) {
                url = urls.get("2").getAsString();
            } else {
                url = urls.get("1").getAsString();
            }

            url = url.replaceFirst("//", "https://");

            emotesMap.put(name, url);
        }
        } catch (IOException | NullPointerException e) {
            System.out.println("[ERROR}Не удалось получить FFZ-смайлы");
        } finally {
            if (channelResponse != null) {
                try {
                    channelResponse.close();
                } catch (final Throwable th) {
                    System.out.println(th.getMessage());
                }
            }
        }

        return emotesMap;
    }

    public Map<String, String> getGlobalTwitchEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();

        String urlTemplate = "https://static-cdn.jtvnw.net/emoticons/v1/{{id}}/{{size}}";
        String globalEmotesResource = "https://api.twitchemotes.com/api/v4/channels/0";

        Request request = new Request.Builder()
                .url(globalEmotesResource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .build();
        Response globalResponse = client.newCall(request).execute();

        JsonArray globalEmotes = JsonParser.parseString(globalResponse.body().string())
                .getAsJsonObject()
                .getAsJsonArray("emotes");

        for (var elem : globalEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String id = emoteInfo.get("id").getAsString();
            String size = "3.0";
            String code = emoteInfo.get("code").getAsString();
            if(code.contains("&lt")) {
                code = code.replaceAll("&lt\\\\;", "<");
            } else if (code.contains("&gt")) {
                code = code.replaceAll("&gt\\\\;", ">");
            }
            String url = urlTemplate
                    .replace("{{id}}", id)
                    .replace("{{size}}", size);
            emotesMap.put(code, url);
        }

        return emotesMap;
    }

    public Map<String, String> getTwitchEmotes(String nick){
        Map<String, String> emotesMap = new HashMap<>();

        String urlTemplate = "https://static-cdn.jtvnw.net/emoticons/v1/{{id}}/{{size}}";
        String channelIdResource = "https://api.twitch.tv/v5/users?login=" + nick;

        Request request = new Request.Builder()
                .url(channelIdResource)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .get()
                .addHeader("Client-ID", Config.getStringFor("UNOFFICIAL_TWITCH_CID"))
                .build();
        Response channelIDResponse = null;
        try {
            channelIDResponse = client.newCall(request).execute();

        String channelid = JsonParser.parseString(channelIDResponse.body().string())
                .getAsJsonObject()
                .getAsJsonArray("users")
                .get(0)
                .getAsJsonObject()
                .get("_id")
                .getAsString();


        if (channelid.equals("")) {
            throw new IOException();
        }

        String channelEmotesResource = "https://api.twitchemotes.com/api/v4/channels/" + channelid;
        Request channelRequest = new Request.Builder()
                .url(channelEmotesResource)
                .get()
                .addHeader("Cookie", "__cfduid=d7d0db7130bf26e37b2449d52460622361598906796")
                .build();
        Response channelResponse = client.newCall(channelRequest).execute();
        if (channelResponse.body().contentLength() <= 0) {
            channelResponse.body().close();
            throw new IOException();
        }
        JsonArray channelEmotes = JsonParser.parseString(channelResponse.body().string())
                .getAsJsonObject()
                .getAsJsonArray("emotes");


        if (channelEmotes == null) {
            throw new IOException();
        }
        for (var elem : channelEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String id = emoteInfo.get("id").getAsString();
            String size = "3.0";
            String code = emoteInfo.get("code").getAsString();
            String url = urlTemplate
                    .replace("{{id}}", id)
                    .replace("{{size}}", size);
            emotesMap.put(code, url);
        }

        } catch (IOException | NullPointerException e) {
            channelIDResponse.body().close();
            System.out.println("[ERROR]Не удалось получить Twitch-смайлы");
        }

        return emotesMap;
    }

    public String getTwitchEmoteURL(String emoteID) {
        String urlTemplate = "https://static-cdn.jtvnw.net/emoticons/v1/{{id}}/{{size}}";
        String size = "3.0";
        String url = urlTemplate
                .replace("{{id}}", emoteID)
                .replace("{{size}}", size);
        return url;
    }
}
