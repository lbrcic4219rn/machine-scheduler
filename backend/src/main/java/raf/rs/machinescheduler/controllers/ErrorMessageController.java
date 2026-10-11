package raf.rs.machinescheduler.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import raf.rs.machinescheduler.model.ErrorMessage;
import raf.rs.machinescheduler.repositories.ErrorMessageRepo;
import raf.rs.machinescheduler.services.UserService;

import java.util.List;

@RestController
@RequestMapping("api/logs")
@RequiredArgsConstructor
public class ErrorMessageController {
    private final ErrorMessageRepo errorMessageRepo;
    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ErrorMessage>> getAll() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(username)
                .map(user -> ResponseEntity.ok(errorMessageRepo.findByUserId(user.getId())))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
