package api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.TwitchEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.webhooks.domain.WebhookRequest;
import com.github.twitch4j.helix.webhooks.topics.TwitchWebhookTopic;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.github.twitch4j.pubsub.TwitchPubSub;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.domain.PubSubRequest;
import com.github.twitch4j.pubsub.enums.PubSubType;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import constants.CommandConstants;
import constants.Config;
import controllers.MainController;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchBot {
    TwitchClient twitchClient;
    IDisposable handlerMessages;
    boolean isClosed = false;
    OAuth2Credential credential;


    public TwitchBot(OAuth2Credential credential) {
        Logger.getLogger(TwitchClient.class.getName()).setLevel(Level.WARNING);
        this.credential = credential;
        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .withClientId(Config.getStringFor("TWITCH_CID"))
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .withDefaultAuthToken(credential)
                .build();
        //twitchClient.getClientHelper().setDefaultAuthToken(credential);
        SimpleEventHandler eventHandler = twitchClient.getEventManager()
                .getEventHandler(SimpleEventHandler.class);

        eventHandler.onEvent(ChannelMessageEvent.class, this::handlerMethod);
        



        eventHandler.onEvent(ChannelGoLiveEvent.class, event -> {
            System.out.println("[LIVE]" + "[" + event.getChannel().getName() + "]");
//            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!выкл фан");
//            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!задержка фан 30");
            MainController.addListeningChannel(event.getChannel().getName());
            MainController.goLive(event.getChannel().getName());
        });

        eventHandler.onEvent(ChannelGoOfflineEvent.class, event -> {
            System.out.println("[OFFLINE]" + "[" + event.getChannel().getName() + "]");
//            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!вкл фан");
//            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!задержка фан 5");
            MainController.goOffline(event.getChannel().getName());
        });

        eventHandler.onEvent(FollowEvent.class, followEvent -> {
            System.out.println("[FOLLOW][" + followEvent.getChannel().getName() + "][" + followEvent.getUser().getName() + "]");
        });


        


    }

    boolean isStarted = false;

     void handlerMethod(ChannelMessageEvent event) {

        if(isClosed) {
            close();
            return;
        }

         IRCMessageEvent messageEvent = event.getMessageEvent();

        /*
        long timeReconnect = System.currentTimeMillis();

        if((timeReconnect - lastReconnect) > 1000*60*10) {
            twitchClient.getChat().reconnect();
        }

         */
         Set<String> permissions = new HashSet<>();
         event.getPermissions().forEach(t -> permissions.add(t.toString()));
         String channelname = event.getChannel().getName();

         String username = event.getUser().getName();
         String message = event.getMessage();

         if (CommandConstants.masterNames.contains(username)) {
             permissions.add("MASTER");
         }

        String logMessage = ("[" + event.getFiredAt().getTime() + "][" + channelname + "]["
                     + permissions.toString()+"] "
                     + username + ": "
                     + message);

        //controllers.MainController.writeToLogs(logMessage);
        System.out.println(logMessage);


         new Thread(() -> MainController.handleMessage(channelname, username, permissions, message, messageEvent)).start();

         //MainController.handleMessage(channelname, username, permissions, message);

     }


     public void close() {
         isClosed = true;
         twitchClient.getClientHelper().close();
         twitchClient.close();
     }

     public void deleteMessage(String channel, String id){
         twitchClient.getChat().delete(channel, id);
     }

    public void sendMessage (String message, String channelName) {
        System.out.println("[LOGS][" + Calendar.getInstance().getTime() + "][" + channelName +"][SEND_MESSAGE]:" + message);
        twitchClient.getChat().sendMessage(channelName, message);
    }

    public void sendMessage (String message, String channelName, boolean isLighting) {
        System.out.println("[LOGS][" + Calendar.getInstance().getTime() + "][" + channelName +"][SEND_MESSAGE]:" + message);
        if (isLighting) {
            twitchClient.getChat().sendMessage(channelName, "/me " + message);
        } else {
            twitchClient.getChat().sendMessage(channelName, message);
        }

    }

    public void sendMessagePm(String message, String user){
        System.out.println("[LOGS][" + new Date() + "][" + user +"][SEND_MESSAGE_PM]:" + message);
        twitchClient.getChat().sendPrivateMessage(user, message);
    }




    public String joinToChannel(String channel) {
        twitchClient.getChat().joinChannel(channel);
        twitchClient.getClientHelper().enableStreamEventListener(channel);
        MainController.addListeningChannel(channel);
        String message = "Присоединился к каналу: \"" + channel + "\"";
        sendMessage(message, "martellx");
        twitchClient.getClientHelper().enableFollowEventListener(channel);
        return null;
    }



    public TwitchClient getTwitchClient() {
        return twitchClient;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchClient.close();
    }
}

