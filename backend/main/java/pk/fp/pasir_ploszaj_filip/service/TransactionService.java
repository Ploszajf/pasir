package pk.fp.pasir_ploszaj_filip.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pk.fp.pasir_ploszaj_filip.dto.TransactionDTO;
import pk.fp.pasir_ploszaj_filip.model.Transaction;
import pk.fp.pasir_ploszaj_filip.model.TransactionType;
import pk.fp.pasir_ploszaj_filip.model.User;
import pk.fp.pasir_ploszaj_filip.repository.TransactionRepository;
import pk.fp.pasir_ploszaj_filip.repository.UserRepository;
import pk.fp.pasir_ploszaj_filip.dto.BalanceDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika"));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findByUser(getCurrentUser());
    }

    public Transaction getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono transakcji o ID " + id));
        User user = getCurrentUser();
        if (!transaction.getUser().getEmail().equals(user.getEmail())) {
            throw new AccessDeniedException("Brak uprawnień");
        }
        return transaction;
    }

    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setUser(getCurrentUser());
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, TransactionDTO transactionDTO) {
        Transaction transaction = getTransactionById(id);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono transakcji o ID " + id));

        User user = getCurrentUser();
        if (!transaction.getUser().getEmail().equals(user.getEmail())) {
            throw new AccessDeniedException("Brak uprawnień do usunięcia tej transakcji");
        }
        transactionRepository.delete(transaction);
    }

    public BalanceDTO getUserBalance(User user, Double days) {
        List<Transaction> userTransactions = transactionRepository.findByUser(user);

        if (days != null && days > 0) {
            long seconds = (long) (days * 24 * 60 * 60);
            LocalDateTime cutoffDate = LocalDateTime.now().minusSeconds(seconds);
            userTransactions = userTransactions.stream()
                    .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(cutoffDate))
                    .toList();
        }

        double income = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double expense = userTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
        return new BalanceDTO(income, expense, income - expense);
    }
}