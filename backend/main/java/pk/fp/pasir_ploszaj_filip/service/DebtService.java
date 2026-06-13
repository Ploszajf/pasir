package pk.fp.pasir_ploszaj_filip.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pk.fp.pasir_ploszaj_filip.dto.DebtDTO;
import pk.fp.pasir_ploszaj_filip.model.Debt;
import pk.fp.pasir_ploszaj_filip.model.Group;
import pk.fp.pasir_ploszaj_filip.model.Membership;
import pk.fp.pasir_ploszaj_filip.model.User;
import pk.fp.pasir_ploszaj_filip.repository.DebtRepository;
import pk.fp.pasir_ploszaj_filip.repository.GroupRepository;
import pk.fp.pasir_ploszaj_filip.repository.MembershipRepository;

import java.util.List;

@Service
public class DebtService {
    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;
    private final MembershipService membershipService;
    private final MembershipRepository membershipRepository;
    private final CurrentUserService currentUserService;

    public DebtService(
            DebtRepository debtRepository,
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            MembershipService membershipService,
            CurrentUserService currentUserService) {
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.membershipService = membershipService;
        this.currentUserService = currentUserService;
    }

    public List<Debt> getGroupDebts(Long groupId) {
        membershipService.assertCurrentUserIsGroupMember(groupId);
        return debtRepository.findByGroupId(groupId);
    }

    public Debt createDebt(DebtDTO debtDTO) {
        Group group = groupRepository.findById(debtDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można utworzyć długu. Grupa o ID " + debtDTO.getGroupId() + " nie istnieje."));

        // POPRAWKA: Szukamy członkostwa łącząc ID grupy i ID użytkownika (dłużnika)
        Membership debtorMembership = membershipRepository.findByGroupIdAndUserId(group.getId(), debtDTO.getDebtorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Użytkownik o ID " + debtDTO.getDebtorId() + " nie jest członkiem grupy o ID " + group.getId()));

        // POPRAWKA: Szukamy członkostwa łącząc ID grupy i ID użytkownika (wierzyciela)
        Membership creditorMembership = membershipRepository.findByGroupIdAndUserId(group.getId(), debtDTO.getCreditorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Użytkownik o ID " + debtDTO.getCreditorId() + " nie jest członkiem grupy o ID " + group.getId()));

        System.out.println("Tworzenie długu dla użytkowników: " + debtDTO.getDebtorId() + " oraz " + debtDTO.getCreditorId());

        User debtor = debtorMembership.getUser();
        User creditor = creditorMembership.getUser();

        membershipService.assertCurrentUserIsGroupMember(group.getId());
        membershipService.assertUserIsGroupMember(group.getId(), debtor.getId());
        membershipService.assertUserIsGroupMember(group.getId(), creditor.getId());

        if (debtor.getId().equals(creditor.getId())) {
            throw new IllegalStateException("Dłużnik i wierzyciel muszą być różnymi użytkownikami.");
        }

        User currentUser = currentUserService.getCurrentUser();
        assertCurrentUserCanManageDebt(group, debtor, creditor, currentUser);

        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);
        debt.setAmount(debtDTO.getAmount());
        debt.setTitle(debtDTO.getTitle());
        return debtRepository.save(debt);
    }

    public void deleteDebt (Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie można usunąć długu. Dług o ID " + debtId + " nie istnieje."));
        membershipService.assertCurrentUserIsGroupMember(debt.getGroup().getId());
        User currentUser = currentUserService.getCurrentUser();
        assertCurrentUserCanManageDebt(debt.getGroup(), debt.getDebtor(), debt.getCreditor(), currentUser);
        debtRepository.delete(debt);
    }

    public Debt markDebtAsPaid (Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();
        if (!debt.getDebtor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko dluznik moze oznaczyc dlug jako oplacony.");
        }
        debt.setPaidByDebtor(true);
        debt.setConfirmedByCreditor(false);
        return debtRepository.save(debt);
    }

    public Debt confirmDebtPayment (Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();
        if (!debt.getCreditor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko wierzyciel moze potwierdzic splate dlugu.");
        }
        if (!debt.isPaidByDebtor()) {
            throw new IllegalStateException("Dlug musi zostać najpierw oznaczony jako oplacony przez dluznika.");
        }
        debt.setConfirmedByCreditor(true);
        return debtRepository.save(debt);
    }

    private Debt getDebtForCurrentGroupMember(Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Nie znaleziono dlugu o ID " + debtId + "."));
        membershipService.assertCurrentUserIsGroupMember(debt.getGroup().getId());
        return debt;
    }

    private void assertCurrentUserCanManageDebt(Group group, User debtor, User creditor, User currentUser){
        boolean isGroupOwner = group.getOwner().getId().equals(currentUser.getId());
        boolean isDebtParticipant = debtor.getId().equals(currentUser.getId())
                || creditor.getId().equals(currentUser.getId());

        if (!isGroupOwner && !isDebtParticipant) {
            throw new AccessDeniedException(
                    "Tylko właścicel grupy albo uczestnik długu może wykonać tę operację.");
        }
    }
}