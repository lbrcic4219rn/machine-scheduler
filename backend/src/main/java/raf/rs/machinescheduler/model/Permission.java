package raf.rs.machinescheduler.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Permission {
    private int canCreateUser;
    private int canReadUser;
    private int canUpdateUser;
    private int canDeleteUser;
    private int canSearchMachine;
    private int canStartMachine;
    private int canStopMachine;
    private int canRestartMachine;
    private int canCreateMachine;
    private int canDestroyMachine;
}
