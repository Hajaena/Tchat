package com.example.app.web;

import com.example.app.entity.ChatMessage;
import com.example.app.service.MessageService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.inject.spi.CDI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ServerEndpoint("/ws/tchat")
public class TchatEndpoint {

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private MessageService service() {
        return CDI.current().select(MessageService.class).get();
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

    // @OnMessage
    // public void onMessage(String json, Session session) {
    // try (JsonReader reader = Json.createReader(new StringReader(json))) {
    // JsonObject obj = reader.readObject();
    // String author = obj.getString("author", "").trim();
    // String text = obj.getString("text", "").trim();
    // if (author.isEmpty() || text.isEmpty()) return;

    // ChatMessage m = service().saveMessage(author, text);
    // String iso =
    // m.getSentAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(ISO);

    // JsonObject out = Json.createObjectBuilder()
    // .add("id", m.getId())
    // .add("author", m.getStudent().getName())
    // .add("text", m.getContent())
    // .add("sentAt", iso)
    // .build();
    // broadcast(out.toString());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    @OnMessage
    public void onMessage(String json, Session session) {
        System.out.println("WS msg: " + json);
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject obj = reader.readObject();
            String author = obj.getString("author", "").trim();
            String text = obj.getString("text", "").trim();
            if (author.isEmpty() || text.isEmpty())
                return;

            // 1) Diffuse tout de suite pour valider le pipeline WS -> client
            String nowIso = java.time.LocalDateTime.now().format(ISO);
            JsonObject out = Json.createObjectBuilder()
                    .add("author", author).add("text", text).add("sentAt", nowIso)
                    .build();
            broadcast(out.toString());

            // 2) Persiste en best-effort
            try {
                ChatMessage m = service().saveMessage(author, text);
                System.out.println("Saved msg id=" + m.getId());
            } catch (Exception pe) {
                pe.printStackTrace(); // problème CDI/JTA/JPA ici
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        for (Session s : sessions)
            if (s.isOpen())
                s.getAsyncRemote().sendText(message);
    }
}
