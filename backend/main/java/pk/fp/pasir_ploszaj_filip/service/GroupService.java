package pk.fp.pasir_ploszaj_filip.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pk.fp.pasir_ploszaj_filip.dto.GroupDTO;
import pk.fp.pasir_ploszaj_filip.model.Group;
import pk.fp.pasir_ploszaj_filip.model.Membership;
import pk.fp.pasir_ploszaj_filip.model.User;
import pk.fp.pasir_ploszaj_filip.repository.DebtRepository;
import pk.fp.pasir_ploszaj_filip.repository.GroupRepository;
import pk.fp.pasir_ploszaj_filip.repository.MembershipRepository;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final CurrentUserService currentUserService;

    public GroupService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            DebtRepository debtRepository,
            CurrentUserService currentUserService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.currentUserService = currentUserService;
    }

    public List<Group> getAllGroups() {
        User currentUser = currentUserService.getCurrentUser();
        return groupRepository.findByMemberships_User(currentUser);
    }

    @Transactional
    public Group createGroup(GroupDTO groupDTO) {
        User owner = currentUserService.getCurrentUser();
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setOwner(owner);

        Group savedGroup = groupRepository.save(group);

        Membership membership = new Membership();
        membership.setUser(owner);
        membership.setGroup(savedGroup);

        if (savedGroup.getMemberships() == null) {
            savedGroup.setMemberships(new java.util.ArrayList<>());
        }
        savedGroup.getMemberships().add(membership);

        membershipRepository.save(membership);

        return savedGroup;
    }

    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie można usunąć grupy. Grupa o ID " + id + "nie istnieje."));

        User currentUser = currentUserService.getCurrentUser();
        if (!group.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Tylko właściciel grupy może ją usunąć.");
        }
        debtRepository.deleteByGroupId(id);
        membershipRepository.deleteByGroupId(id);
        groupRepository.delete(group);
    }
}