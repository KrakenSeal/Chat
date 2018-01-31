package com.ezhov.server;

import com.ezhov.connector.ChatConnector;
import com.ezhov.connector.ChatListener;
import com.ezhov.connector.ConnectorSettings;
import com.ezhov.connector.SocketChatListener;
import com.ezhov.domain.ChatClient;
import com.ezhov.domain.ChatMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerImpl implements ChatServer {

    Boolean isStarted;
    ChatListener chatListener;
    ConnectorSettings settings;
    List<ChatMessage> messages;
    List<ChatClient> clients;


    public ChatServerImpl() {
        settings = new ConnectorSettings(8989, "127.0.0.1");
        chatListener = new SocketChatListener(settings);
        isStarted = false;
        messages = new LinkedList<>();
        clients = new LinkedList<>();
    }

    @Override
    public synchronized void addMessage(ChatMessage chatMessage) {
        messages.add(chatMessage);
        for (ChatClient client: clients) {
          client.sendMessage(chatMessage);
        }
    }

    public synchronized void addClient(ChatClient client){
        clients.add(client);
    }

    private void clientListen() {
        while (isStarted) {
            try {
                ChatConnector connector = chatListener.waitClient();
                ChatClient chatClient = new ChatClient(this,connector);
                addClient(chatClient);
                chatClient.start();
            } catch (IOException ex) {
                Logger.getLogger(ChatServerImpl.class.getName()).log(Level.SEVERE, "Occured error during established server connection" + ex);
            }


        }
    }


    @Override
    public void run() {
        try {
            chatListener.connect();
            isStarted = true;
            Thread listener = new Thread(){
                @Override
                public void run(){
                    clientListen();
                }
            };
            listener.start();
        } catch (IOException ex) {
            Logger.getLogger(ChatServerImpl.class.getName()).log(Level.SEVERE, "Occured error during established server connection" + ex);
        }
    }

    @Override
    public void stop() {
        isStarted = false;
    }
}