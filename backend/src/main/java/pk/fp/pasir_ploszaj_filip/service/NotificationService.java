package pk.fp.pasir_ploszaj_filip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pk.fp.pasir_ploszaj_filip.dto.ExpenseNotificationDTO;
import pk.fp.pasir_ploszaj_filip.model.Debt;
import pk.fp.pasir_ploszaj_filip.model.User;
import pk.fp.pasir_ploszaj_filip.websocket.NotificationWebSocketHandler;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    public void triggerExpenseNotification(Debt debt, User currentUser) {
        User debtor = debt.getDebtor();
        if (debtor.getId().equals(currentUser.getId())) {
            return;
        }

        String creatorEmail = currentUser.getEmail();
        String groupName = debt.getGroup().getName();
        String title = debt.getTitle();

        String messageText = String.format("%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                creatorEmail, title, groupName, debt.getAmount());

        ExpenseNotificationDTO notificationDTO = new ExpenseNotificationDTO(
                debt.getGroup().getId(),
                groupName,
                title,
                debt.getAmount(),
                debt.getAmount(),
                creatorEmail,
                messageText
        );

        try {
            String jsonPayload = objectMapper.writeValueAsString(notificationDTO);
            webSocketHandler.sendNotification(debtor.getEmail(), jsonPayload);
        } catch (JsonProcessingException e) {
            System.err.println("Błąd generowania JSON dla WebSocket: " + e.getMessage());
        }
    }

    public void notifyGroupMembers(
            Map<String, Double> userShares,
            Long groupId,
            String groupName,
            String title,
            Double totalAmount,
            Long creatorId,
            String creatorEmail) {

        for (Map.Entry<String, Double> entry : userShares.entrySet()) {
            String participantEmail = entry.getKey();
            Double share = entry.getValue();

            if (participantEmail.equals(creatorEmail)) {
                continue;
            }

            String messageText = String.format("%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                    creatorEmail, title, groupName, share);

            ExpenseNotificationDTO notificationDTO = new ExpenseNotificationDTO(
                    groupId,
                    groupName,
                    title,
                    BigDecimal.valueOf(totalAmount),
                    BigDecimal.valueOf(share),
                    creatorEmail,
                    messageText
            );

            try {
                String jsonPayload = objectMapper.writeValueAsString(notificationDTO);
                webSocketHandler.sendNotification(participantEmail, jsonPayload);
            } catch (JsonProcessingException e) {
                System.err.println("Błąd generowania JSON dla WebSocket: " + e.getMessage());
            }
        }
    }
}