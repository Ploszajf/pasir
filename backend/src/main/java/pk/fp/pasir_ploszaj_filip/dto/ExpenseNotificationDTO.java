package pk.fp.pasir_ploszaj_filip.dto;

import java.math.BigDecimal;

public class ExpenseNotificationDTO {
    private final String type = "GROUP_EXPENSE_ADDED";
    private Long groupId;
    private String groupName;
    private String title;
    private BigDecimal amount;
    private BigDecimal userShare;
    private String createdByEmail;
    private String message;

    // Główny konstruktor dla klas używających BigDecimal (np. DebtService)
    public ExpenseNotificationDTO(Long groupId, String groupName, String title,
                                  BigDecimal amount, BigDecimal userShare,
                                  String createdByEmail, String message) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.title = title;
        this.amount = amount;
        this.userShare = userShare;
        this.createdByEmail = createdByEmail;
        this.message = message;
    }

    public ExpenseNotificationDTO(Long groupId, String groupName, String title,
                                  Double amount, Double userShare,
                                  String createdByEmail, String message) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.title = title;
        this.amount = amount != null ? BigDecimal.valueOf(amount) : null;
        this.userShare = userShare != null ? BigDecimal.valueOf(userShare) : null;
        this.createdByEmail = createdByEmail;
        this.message = message;
    }

    public String getType() { return type; }
    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getUserShare() { return userShare; }
    public String getCreatedByEmail() { return createdByEmail; }
    public String getMessage() { return message; }
}