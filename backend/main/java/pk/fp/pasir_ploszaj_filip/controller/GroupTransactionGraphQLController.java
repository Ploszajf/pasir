package pk.fp.pasir_ploszaj_filip.controller;

import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import pk.fp.pasir_ploszaj_filip.dto.GroupTransactionDTO;
import pk.fp.pasir_ploszaj_filip.model.User;
import pk.fp.pasir_ploszaj_filip.service.CurrentUserService;
import pk.fp.pasir_ploszaj_filip.service.GroupTransactionService;

@Controller
public class GroupTransactionGraphQLController {

    private final GroupTransactionService groupTransactionService;
    private final CurrentUserService currentUserService;


    public GroupTransactionGraphQLController(
            GroupTransactionService groupTransactionService,
            CurrentUserService currentUserService) {
        this.groupTransactionService = groupTransactionService;
        this.currentUserService = currentUserService;
    }

    @MutationMapping
    public Boolean addGroupTransaction(@Valid @Argument GroupTransactionDTO groupTransactionDTO) {
        User user = currentUserService.getCurrentUser();
        groupTransactionService.addGroupTransaction(groupTransactionDTO, user);
        return true;
    }
}