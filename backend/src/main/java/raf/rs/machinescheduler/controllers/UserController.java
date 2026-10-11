package raf.rs.machinescheduler.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.PermissionEnum;
import raf.rs.machinescheduler.requests.CreateUserRequest;
import raf.rs.machinescheduler.responses.UserResponse;
import raf.rs.machinescheduler.services.UserService;
import raf.rs.machinescheduler.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionsUtil permissionsUtil;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_READ_USER))
            return ResponseEntity.status(403).build();

        List<User> foundUsers = userService.findAll();
        List<UserResponse> resp = new ArrayList<>();
        for (User u : foundUsers) {
            resp.add(new UserResponse(u.getId(), u.getName(), u.getSurname(), u.getEmail(), u.getPermission()));
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest user) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_CREATE_USER))
            return ResponseEntity.status(403).build();

        Optional<User> existingUser = userService.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("User with that email already exists");
        }

        User newUser = new User(
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                this.passwordEncoder.encode(user.getPassword()),
                user.getPermission()
        );

        userService.save(newUser);

        UserResponse response = new UserResponse(
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPermission()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_DELETE_USER))
            return ResponseEntity.status(403).build();

        Optional<User> toDelete = userService.findById(Long.parseLong(id));
        if (toDelete.isPresent()) {
            this.userService.delete(Long.parseLong(id));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(404).body("User with given email not found");
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_READ_USER))
            return ResponseEntity.status(403).build();

        Optional<User> found = this.userService.findById(Long.parseLong(id));
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body("User with given id not found");
        }

        User user = found.get();
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getSurname(), user.getEmail(), user.getPermission());
        return ResponseEntity.ok().body(userResponse);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUser(@PathVariable String id, @RequestBody CreateUserRequest updateData) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_UPDATE_USER))
            return ResponseEntity.status(403).build();

        Optional<User> user = this.userService.findById(Long.parseLong(id));
        if (user.isPresent()) {
            User u = user.get();
            u.setEmail(updateData.getEmail());
            u.setName(updateData.getName());
            u.setSurname(updateData.getSurname());
            u.setPermission(updateData.getPermission());
            this.userService.save(u);
            return ResponseEntity.ok().body(new UserResponse(
                    u.getName(),
                    u.getSurname(),
                    u.getEmail(),
                    u.getPermission()));
        }
        return ResponseEntity.status(404).body("User with given id not found");
    }
}
