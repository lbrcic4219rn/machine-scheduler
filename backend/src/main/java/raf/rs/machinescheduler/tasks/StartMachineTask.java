package raf.rs.machinescheduler.tasks;

import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.repositories.ErrorMessageRepo;
import raf.rs.machinescheduler.repositories.MachineRepo;
import raf.rs.machinescheduler.services.UserService;

public class StartMachineTask extends MachineTask {

    public StartMachineTask(Long machineId, User client, MachineRepo machineRepo, ErrorMessageRepo errorMessageRepo, UserService userService) {
        super(machineId, client, machineRepo, errorMessageRepo, userService);
    }

    @Override
    protected String operationName() {
        return "START";
    }

    @Override
    protected String rejectionReason(Status status) {
        return status == Status.RUNNING ? "Machine already running" : null;
    }

    @Override
    protected void execute() throws InterruptedException {
        transitionTo(Status.RUNNING, false);
    }
}
