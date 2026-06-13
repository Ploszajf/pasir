package pk.fp.pasir_ploszaj_filip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "debts")
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String title;

    private boolean paidByDebtor = false;
    private boolean confirmedByCreditor = false;

    public String getTitle() { return title != null ? title : "Brak opisu";}

    public Long getDebtorId() {
        return debtor != null ? debtor.getId() : null;
    }

    public Long getCreditorId() {
        return creditor != null ? creditor.getId() : null;
    }

    @ManyToOne
    @JoinColumn(name = "debtor_id")
    private User debtor;

    @ManyToOne
    @JoinColumn(name = "creditor_id")
    private User creditor;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
}