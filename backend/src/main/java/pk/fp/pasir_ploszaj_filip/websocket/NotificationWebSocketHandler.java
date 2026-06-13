package pk.fp.pasir_ploszaj_filip.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userEmail = (String) session.getAttributes().get("userEmail");
        if (userEmail != null) {
            userSessions.put(userEmail, session);
            System.out.println("WebSocket połączony dla użytkownika: " + userEmail);
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Brak autoryzacji"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userEmail = (String) session.getAttributes().get("userEmail");
        if (userEmail != null) {
            userSessions.remove(userEmail);
            System.out.println("WebSocket rozłączony dla użytkownika: " + userEmail);
        }
    }

    public void sendNotification(String userEmail, String jsonPayload) {
        WebSocketSession session = userSessions.get(userEmail);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(jsonPayload));
            } catch (IOException e) {
                System.err.println("Błąd wysyłania wiadomości do: " + userEmail + " - " + e.getMessage());
            }
        }
    }
}