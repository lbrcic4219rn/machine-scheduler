package raf.rs.machinescheduler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import raf.rs.machinescheduler.model.ErrorMessage;
import raf.rs.machinescheduler.model.Machine;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.repositories.ErrorMessageRepo;
import raf.rs.machinescheduler.repositories.MachineRepo;
import raf.rs.machinescheduler.services.UserService;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
public abstract class MachineTask implements Runnable {
    private static final long MIN_DELAY_MS = 10_000;
    private static final long MAX_DELAY_MS = 15_000;

    private final Long machineId;
    private final User client;
    private final MachineRepo machineRepo;
    private final ErrorMessageRepo errorMessageRepo;
    private final UserService userService;

    public static void simulateWork() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(MIN_DELAY_MS, MAX_DELAY_MS));
    }

    protected abstract String operationName();

    protected abstract String rejectionReason(Status status);

    protected abstract void execute() throws InterruptedException;

    @Override
    public void run() {
        log.info("Scheduled {} of machine with id: {}", operationName(), machineId);

        if (userService.findById(client.getId()).isEmpty()) {
            return;
        }

        Optional<Machine> machine = machineRepo.findById(machineId);
        if (machine.isEmpty()) {
            return;
        }

        Machine m = machine.get();
        String rejection = rejectionReason(m.getStatus());
        if (rejection != null) {
            logError(rejection);
            return;
        }

        if (m.isOperationActive()) {
            logError("Machine was undergoing operation");
            return;
        }

        m.setOperationActive(true);
        machineRepo.save(m);
        try {
            execute();
            log.info("Scheduled {} of machine with id: {} successful", operationName(), machineId);
        } catch (InterruptedException e) {
            logError("Operation was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    protected void transitionTo(Status status, boolean operationActive) throws InterruptedException {
        simulateWork();
        machineRepo.findById(machineId).ifPresent(m -> {
            m.setStatus(status);
            m.setOperationActive(operationActive);
            machineRepo.save(m);
        });
    }

    private void logError(String message) {
        errorMessageRepo.save(new ErrorMessage(Instant.now(), machineId, client, operationName(), message));
        log.info("Error message added to db: {}", message);
    }
}
