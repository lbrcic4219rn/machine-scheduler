package raf.rs.machinescheduler.tasks;

import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.repositories.ErrorMessageRepo;
import raf.rs.machinescheduler.repositories.MachineRepo;
import raf.rs.machinescheduler.services.UserService;

public class StopMachineTask extends MachineTask {

    public StopMachineTask(Long machineId, User client, MachineRepo machineRepo, ErrorMessageRepo errorMessageRepo, UserService userService) {
        super(machineId, client, machineRepo, errorMessageRepo, userService);
    }

    @Override
    protected String operationName() {
        return "STOP";
    }

    @Override
    protected String rejectionReason(Status status) {
        return status == Status.STOPPED ? "Machine already stopped" : null;
    }

    @Override
    protected void execute() throws InterruptedException {
        transitionTo(Status.STOPPED, false);
    }
}
