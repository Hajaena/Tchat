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
import java.time.format.DateTimeFormatter;

@ServerEndpoint("/ws/tchat")
public class TchatEndpoint {

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private MessageService service() {
        System.out.println("📌 [DEBUG] Tentative récupération MessageService via CDI...");
        try {
            MessageService svc = CDI.current().select(MessageService.class).get();
            System.out.println("✅ [DEBUG] MessageService obtenu: " + svc);
            return svc;
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] ÉCHEC récupération MessageService!");
            e.printStackTrace();
            throw e;
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("🔌 [DEBUG] Client connecté: " + session.getId() + " | Total clients: " + sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println(
                "🔌 [DEBUG] Client déconnecté: " + session.getId() + " | Clients restants: " + sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        System.err.println("💥 [DEBUG] Erreur WebSocket pour session: " + session.getId());
        thr.printStackTrace();
    }

    @OnMessage
    public void onMessage(String json, Session session) {
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("📨 [1/7] Message WS reçu: " + json);
        System.out.println("   Session: " + session.getId());

        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            System.out.println("📖 [2/7] Parsing JSON...");
            JsonObject obj = reader.readObject();

            String author = obj.getString("author", "").trim();
            String text = obj.getString("text", "").trim();
            System.out.println("   author = '" + author + "'");
            System.out.println("   text = '" + text + "'");

            if (author.isEmpty() || text.isEmpty()) {
                System.out.println("⚠️  [3/7] Champs vides détectés, abandon du message");
                System.out.println("═══════════════════════════════════════════\n");
                return;
            }

            // 1) Diffuse tout de suite pour valider le pipeline WS -> client
            System.out.println("📡 [3/7] Préparation broadcast immédiat...");
            String nowIso = java.time.LocalDateTime.now().format(ISO);
            JsonObject out = Json.createObjectBuilder()
                    .add("author", author)
                    .add("text", text)
                    .add("sentAt", nowIso)
                    .build();

            System.out.println("   JSON à broadcaster: " + out.toString());
            broadcast(out.toString());
            System.out.println("✅ [4/7] Broadcast terminé");

            // 2) Persiste en best-effort
            System.out.println("💾 [5/7] Tentative de persistance en base...");
            try {
                System.out.println("   → Appel service().saveMessage()");
                ChatMessage m = service().saveMessage(author, text);

                System.out.println("✅ [6/7] MESSAGE SAUVEGARDÉ EN BASE!");
                System.out.println("   ID: " + m.getId());
                System.out.println("   Author: " + m.getStudent().getName());
                System.out.println("   Content: " + m.getContent());
                System.out.println("   SentAt: " + m.getSentAt());

            } catch (Exception pe) {
                System.err.println("❌ [6/7] ÉCHEC PERSISTANCE EN BASE!");
                System.err.println("   Type d'exception: " + pe.getClass().getName());
                System.err.println("   Message: " + pe.getMessage());

                if (pe.getCause() != null) {
                    System.err.println("   Cause racine: " + pe.getCause().getClass().getName());
                    System.err.println("   Message cause: " + pe.getCause().getMessage());
                }

                System.err.println("   Stack trace complète:");
                pe.printStackTrace(System.err);
            }

            System.out.println("✅ [7/7] Traitement message terminé");
            System.out.println("═══════════════════════════════════════════\n");

        } catch (Exception e) {
            System.err.println("💥 [ERROR] Exception globale dans onMessage!");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace(System.err);
            System.out.println("═══════════════════════════════════════════\n");
        }
    }

    private void broadcast(String message) {
        System.out.println("   📡 Broadcasting à " + sessions.size() + " client(s)");
        int success = 0;
        int failed = 0;

        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getAsyncRemote().sendText(message);
                    success++;
                    System.out.println("      ✓ Envoyé à session " + s.getId());
                } catch (Exception e) {
                    failed++;
                    System.err.println("      ✗ Échec envoi à session " + s.getId() + ": " + e.getMessage());
                }
            } else {
                System.out.println("      ⊘ Session fermée: " + s.getId());
            }
        }

        System.out.println("   Résultat: " + success + " succès, " + failed + " échecs");
    }
}