package com.example.prm392_finalproject_productsaleapp_group2.chat.signalr;

import android.util.Log;

import com.example.prm392_finalproject_productsaleapp_group2.net.ApiConfig;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.function.Consumer;

public class SignalRManager {
    private static final String TAG = "SignalRManager";
    private static SignalRManager instance;
    private HubConnection hubConnection;

    private SignalRManager() {}

    public static synchronized SignalRManager getInstance() {
        if (instance == null) {
            instance = new SignalRManager();
        }
        return instance;
    }

    public void connect(int userId,
                        Consumer<Object> onReceiveMessage,
                        Consumer<Object> onMessageSent,
                        Consumer<Object> onError) {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            // already connected, re-register handlers
            registerHandlers(onReceiveMessage, onMessageSent, onError);
            hubConnection.send("RegisterUser", userId);
            return;
        }

        String hubUrl = ApiConfig.BASE_URL + "/chathub"; // adjust to your hub endpoint
        hubConnection = HubConnectionBuilder.create(hubUrl).build();

        registerHandlers(onReceiveMessage, onMessageSent, onError);

        hubConnection.start().doOnComplete(() -> {
            Log.d(TAG, "SignalR connected");
            hubConnection.send("RegisterUser", userId);
        }).doOnError(throwable -> {
            Log.e(TAG, "SignalR connect error", throwable);
            if (onError != null) onError.accept(throwable.getMessage());
        }).blockingAwait();
    }

    private void registerHandlers(Consumer<Object> onReceiveMessage,
                                  Consumer<Object> onMessageSent,
                                  Consumer<Object> onError) {
        hubConnection.on("ReceiveMessage", (message) -> {
            if (onReceiveMessage != null) onReceiveMessage.accept(message);
        }, Object.class);

        hubConnection.on("MessageSent", (message) -> {
            if (onMessageSent != null) onMessageSent.accept(message);
        }, Object.class);

        hubConnection.on("Error", (msg) -> {
            if (onError != null) onError.accept(msg);
        }, String.class);
    }

    public void requestHistory(int userId, int otherUserId, int page, int size) {
        if (hubConnection != null) {
            hubConnection.send("GetChatHistory", userId, otherUserId, page, size);
        }
    }

    public void sendMessage(int senderId, int receiverId, String message) {
        if (hubConnection != null) {
            hubConnection.send("SendMessage", senderId, receiverId, message);
        }
    }

    public void disconnect() {
        if (hubConnection != null) {
            hubConnection.stop();
            hubConnection = null;
        }
    }
}


