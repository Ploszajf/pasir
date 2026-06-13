package pk.fp.pasir_ploszaj_filip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.fp.pasir_ploszaj_filip.model.Group;
import pk.fp.pasir_ploszaj_filip.model.User;
import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMemberships_User(User user);
}