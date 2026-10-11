package raf.rs.machinescheduler.services;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import raf.rs.machinescheduler.model.Machine;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.repositories.ErrorMessageRepo;
import raf.rs.machinescheduler.repositories.MachineRepo;
import raf.rs.machinescheduler.tasks.MachineTask;
import raf.rs.machinescheduler.tasks.RestartMachine;
import raf.rs.machinescheduler.tasks.StartMachineTask;
import raf.rs.machinescheduler.tasks.StopMachineTask;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MachineService {
    private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(Executors.newScheduledThreadPool(10));

    private final MachineRepo machineRepo;
    private final UserService userService;
    private final ErrorMessageRepo errorMessageRepo;

    private static Instant startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static Specification<Machine> matching(Long userId, String name, List<Status> statusList, Instant from, Instant to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.isTrue(root.get("active")));
            if (name != null) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (statusList != null) {
                predicates.add(root.get("status").in(statusList));
            }
            if (from != null) {
                predicates.add(cb.between(root.get("createdAt"), from, to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Optional<User> currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(username);
    }

    public List<Machine> getAllUserMachines() {
        return currentUser()
                .map(u -> machineRepo.findByUserIdAndActive(u.getId(), true))
                .orElseGet(ArrayList::new);
    }

    public Optional<Machine> findOwnedById(Long id) {
        return currentUser().flatMap(u -> machineRepo.findByIdAndUserId(id, u.getId()));
    }

    public Machine createMachine(String name) {
        return currentUser().map(u -> {
            Machine machine = new Machine();
            machine.setUser(u);
            machine.setStatus(Status.STOPPED);
            machine.setActive(true);
            machine.setName(name);
            machine.setCreatedAt(Instant.now());
            machine.setOperationActive(false);
            return machineRepo.save(machine);
        }).orElse(null);
    }

    public void deleteMachine(Long id) {
        findOwnedById(id).ifPresent(m -> {
            m.setActive(false);
            machineRepo.save(m);
        });
    }

    public List<Machine> searchMachines(String name, List<Status> statusList, LocalDate dateFrom, LocalDate dateTo) {
        Optional<User> user = currentUser();
        if (user.isEmpty() || (dateFrom == null) != (dateTo == null)) {
            return new ArrayList<>();
        }
        return machineRepo.findAll(matching(user.get().getId(), name, statusList, startOfDay(dateFrom), startOfDay(dateTo)));
    }

    @Async
    public void startMachine(Long id) {
        runOperation(id, Status.STOPPED, Status.RUNNING);
    }

    @Async
    public void stopMachine(Long id) {
        runOperation(id, Status.RUNNING, Status.STOPPED);
    }

    @Async
    public void restartMachine(Long id) {
        runOperation(id, Status.RUNNING, Status.STOPPED, Status.RUNNING);
    }

    private void runOperation(Long id, Status required, Status... steps) {
        Optional<Machine> machine = machineRepo.findById(id);
        if (machine.isEmpty() || machine.get().getStatus() != required) {
            return;
        }

        Machine m = machine.get();
        m.setOperationActive(true);
        machineRepo.save(m);
        try {
            for (int i = 0; i < steps.length; i++) {
                Status step = steps[i];
                boolean last = i == steps.length - 1;
                MachineTask.simulateWork();
                machineRepo.findById(id).ifPresent(current -> {
                    current.setStatus(step);
                    current.setOperationActive(!last);
                    machineRepo.save(current);
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean canStartMachine(Long id) {
        return machineRepo.findById(id).filter(m -> m.getStatus() == Status.STOPPED).isPresent();
    }

    public boolean canStopMachine(Long id) {
        return machineRepo.findById(id).filter(m -> m.getStatus() == Status.RUNNING).isPresent();
    }

    public boolean isMachineBusy(Long id) {
        return machineRepo.findById(id).filter(Machine::isOperationActive).isPresent();
    }

    public void scheduleMachineStop(Long id, LocalDateTime dateTime) {
        schedule(dateTime, user -> new StopMachineTask(id, user, machineRepo, errorMessageRepo, userService));
    }

    public void scheduleMachineStart(Long id, LocalDateTime dateTime) {
        schedule(dateTime, user -> new StartMachineTask(id, user, machineRepo, errorMessageRepo, userService));
    }

    public void scheduleMachineRestart(Long id, LocalDateTime dateTime) {
        schedule(dateTime, user -> new RestartMachine(id, user, machineRepo, errorMessageRepo, userService));
    }

    private void schedule(LocalDateTime dateTime, Function<User, Runnable> task) {
        currentUser().ifPresent(user ->
                taskScheduler.schedule(task.apply(user), dateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
