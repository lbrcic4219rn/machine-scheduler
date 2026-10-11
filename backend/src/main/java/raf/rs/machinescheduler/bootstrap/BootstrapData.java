package raf.rs.machinescheduler.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import raf.rs.machinescheduler.config.SeedProperties;
import raf.rs.machinescheduler.model.Machine;
import raf.rs.machinescheduler.model.Permission;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.repositories.MachineRepo;
import raf.rs.machinescheduler.repositories.UserRepo;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {
    private static final int SAMPLE_COUNT = 20;

    private final UserRepo userRepo;
    private final MachineRepo machineRepo;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final Random random = new SecureRandom();

    @Override
    public void run(String... args) {
        log.info("Loading data...");

        seedAdmin();
        seedMachines(seedUsers());

        log.info("Data loaded.");
    }

    private void seedAdmin() {
        User adminUser = new User();
        adminUser.setEmail("admin@mail.com");
        adminUser.setName("Pera");
        adminUser.setSurname("Peric");
        adminUser.setPassword(this.passwordEncoder.encode(this.seedProperties.adminPassword()));

        Permission allPerms = new Permission();
        allPerms.setCanCreateUser(1);
        allPerms.setCanReadUser(1);
        allPerms.setCanUpdateUser(1);
        allPerms.setCanDeleteUser(1);
        allPerms.setCanSearchMachine(1);
        allPerms.setCanStartMachine(1);
        allPerms.setCanStopMachine(1);
        allPerms.setCanRestartMachine(1);
        allPerms.setCanCreateMachine(1);
        allPerms.setCanDestroyMachine(1);

        adminUser.setPermission(allPerms);

        userRepo.save(adminUser);
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            User user = new User();
            user.setEmail("user" + i + "@mail.com");
            user.setName("User " + i);
            user.setSurname("Useric");
            user.setPassword(this.passwordEncoder.encode(this.seedProperties.userPasswordPrefix() + i));
            user.setPermission(randomPermission());

            users.add(userRepo.save(user));
        }
        return users;
    }

    private Permission randomPermission() {
        Permission permission = new Permission();
        permission.setCanCreateUser(randomFlag());
        permission.setCanDeleteUser(randomFlag());
        permission.setCanReadUser(randomFlag());
        permission.setCanUpdateUser(randomFlag());
        permission.setCanSearchMachine(randomFlag());
        permission.setCanStartMachine(randomFlag());
        permission.setCanStopMachine(randomFlag());
        permission.setCanRestartMachine(randomFlag());
        permission.setCanCreateMachine(randomFlag());
        permission.setCanDestroyMachine(randomFlag());
        return permission;
    }

    private int randomFlag() {
        return random.nextInt(2);
    }

    private void seedMachines(List<User> owners) {
        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            Machine machine = new Machine();
            machine.setName("Machine " + i);
            machine.setStatus(random.nextBoolean() ? Status.RUNNING : Status.STOPPED);
            machine.setUser(owners.get(random.nextInt(owners.size())));
            machine.setActive(true);
            machine.setCreatedAt(Instant.now());
            machine.setOperationActive(false);
            machineRepo.save(machine);
        }
    }
}
