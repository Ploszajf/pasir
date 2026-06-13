package pk.fp.pasir_ploszaj_filip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import pk.fp.pasir_ploszaj_filip.model.Transaction;
import pk.fp.pasir_ploszaj_filip.model.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);
}