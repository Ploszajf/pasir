package pk.fp.pasir_ploszaj_filip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import pk.fp.pasir_ploszaj_filip.security.JwtUtil;
import pk.fp.pasir_ploszaj_filip.websocket.NotificationWebSocketHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler webSocketHandler;
    private final JwtUtil jwtUtil;

    public WebSocketConfig(NotificationWebSocketHandler webSocketHandler, JwtUtil jwtUtil) {
        this.webSocketHandler = webSocketHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/group-notifications")
                .setAllowedOrigins("http://localhost:5174")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        if (request instanceof ServletServerHttpRequest) {
                            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                            String token = servletRequest.getParameter("token");

                            if (token != null && !token.isEmpty()) {
                                try {
                                    System.out.println("Odebrano token w WebSocket: " + token);

                                    boolean isValid = jwtUtil.validateToken(token);
                                    System.out.println("Czy token jest prawidłowy? " + isValid);

                                    if (isValid) {
                                        String email = jwtUtil.extractUsername(token);
                                        System.out.println("Wyciągnięty email z tokenu: " + email);

                                        if (email != null) {
                                            attributes.put("userEmail", email);
                                            return true;
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Wyjątek podczas weryfikacji tokenu w WebSocket: " + e.getMessage());
                                    e.printStackTrace();
                                    return false;
                                }
                            } else {
                                System.out.println("Brak tokenu w parametrach żądania WebSocket");
                            }
                        }
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {}
                });
    }
}