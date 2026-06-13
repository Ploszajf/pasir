package pk.fp.pasir_ploszaj_filip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.fp.pasir_ploszaj_filip.model.Membership;
import pk.fp.pasir_ploszaj_filip.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);

    Optional<Membership> findByGroupIdAndUserId(Long groupId, Long userId);

    List<Membership> findByUser(User user);
}