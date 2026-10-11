package raf.rs.machinescheduler.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import raf.rs.machinescheduler.model.Machine;
import raf.rs.machinescheduler.model.User;
import raf.rs.machinescheduler.model.enums.PermissionEnum;
import raf.rs.machinescheduler.model.enums.Status;
import raf.rs.machinescheduler.services.MachineService;
import raf.rs.machinescheduler.services.UserService;
import raf.rs.machinescheduler.util.PermissionsUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/machines")
@RequiredArgsConstructor
public class MachineController {

    private static final String MACHINE_BUSY = "Machine is going through an operation";
    private static final String WRONG_STATE = "Machine is not in a correct state";
    private static final DateTimeFormatter SCHEDULE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MachineService machineService;
    private final UserService userService;
    private final PermissionsUtil permissionsUtil;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllUserMachines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByEmail(username);

        if (user.isPresent())
            return ResponseEntity.ok(machineService.getAllUserMachines());

        return ResponseEntity.status(401).build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createMachine(@RequestBody Map<String, String> name) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_CREATE_MACHINE))
            return ResponseEntity.status(403).build();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByEmail(username);


        if (user.isPresent())
            return ResponseEntity.ok(machineService.createMachine(name.get("name")));

        return ResponseEntity.status(401).build();
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteMachine(@PathVariable("id") String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_DESTROY_MACHINE))
            return ResponseEntity.status(403).build();

        Optional<Machine> machine = machineService.findOwnedById(Long.parseLong(id));
        if (machine.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Machine m = machine.get();

        if (machineService.isMachineBusy(m.getId())) {
            return ResponseEntity.status(400).body("Machine is under operation");
        }

        if (m.getStatus() == Status.STOPPED) {
            if (!m.isActive()) {
                return ResponseEntity.status(400).body("Machine is already deleted");
            }
            machineService.deleteMachine(Long.parseLong(id));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(400).body("Machine is not stopped");
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> searchMachine(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateTo
    ) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_SEARCH_MACHINE))
            return ResponseEntity.status(403).build();

        if (status != null) {
            String[] statusArr = status.split(",");
            List<Status> statuses = new ArrayList<>();
            for (String s : statusArr) {
                statuses.add(Status.valueOf(s));
            }
            return ResponseEntity.ok(machineService.searchMachines(name, statuses, dateFrom, dateTo));
        }
        return ResponseEntity.ok(machineService.searchMachines(name, null, dateFrom, dateTo));
    }

    @PatchMapping(value = "start/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> startMachine(@PathVariable("id") String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_START_MACHINE))
            return ResponseEntity.status(403).build();

        if (machineService.findOwnedById(Long.parseLong(id)).isEmpty())
            return ResponseEntity.status(404).build();

        if (machineService.isMachineBusy(Long.parseLong(id))) {
            return ResponseEntity.status(409).body(MACHINE_BUSY);
        }

        if (machineService.canStartMachine(Long.parseLong(id))) {
            machineService.startMachine(Long.parseLong(id));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(400).body(WRONG_STATE);
    }

    @PatchMapping(value = "stop/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> stopMachine(@PathVariable("id") String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_STOP_MACHINE))
            return ResponseEntity.status(403).build();

        if (machineService.findOwnedById(Long.parseLong(id)).isEmpty())
            return ResponseEntity.status(404).build();

        if (machineService.isMachineBusy(Long.parseLong(id))) {
            return ResponseEntity.status(409).body(MACHINE_BUSY);
        }

        if (machineService.canStopMachine(Long.parseLong(id))) {
            machineService.stopMachine(Long.parseLong(id));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(400).body(WRONG_STATE);
    }

    @PatchMapping(value = "restart/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> restartMachine(@PathVariable("id") String id) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_RESTART_MACHINE))
            return ResponseEntity.status(403).build();

        if (machineService.findOwnedById(Long.parseLong(id)).isEmpty())
            return ResponseEntity.status(404).build();

        if (machineService.isMachineBusy(Long.parseLong(id))) {
            return ResponseEntity.status(409).body(MACHINE_BUSY);
        }

        if (machineService.canStopMachine(Long.parseLong(id))) {
            machineService.restartMachine(Long.parseLong(id));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(400).body(WRONG_STATE);
    }

    @PatchMapping(value = "/schedule/start/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> scheduleStartMachine(@PathVariable("id") String id,
                                                       @RequestBody Map<String, String> time) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_START_MACHINE))
            return ResponseEntity.status(403).build();

        LocalDateTime dateTime = LocalDateTime.parse(time.get("time"), SCHEDULE_TIME_FORMAT);

        Optional<Machine> machine = machineService.findOwnedById(Long.parseLong(id));

        if (machine.isPresent()) {
            Machine m = machine.get();
            machineService.scheduleMachineStart(m.getId(), dateTime);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }

    @PatchMapping(value = "/schedule/stop/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> scheduleStopMachine(@PathVariable("id") String id,
                                                      @RequestBody Map<String, String> time) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_STOP_MACHINE))
            return ResponseEntity.status(403).build();

        LocalDateTime dateTime = LocalDateTime.parse(time.get("time"), SCHEDULE_TIME_FORMAT);

        Optional<Machine> machine = machineService.findOwnedById(Long.parseLong(id));

        if (machine.isPresent()) {
            Machine m = machine.get();
            machineService.scheduleMachineStop(m.getId(), dateTime);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }

    @PatchMapping(value = "/schedule/restart/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> scheduleRestartMachine(@PathVariable("id") String id,
                                                         @RequestBody Map<String, String> time) {
        if (this.permissionsUtil.lacksPermission(PermissionEnum.CAN_RESTART_MACHINE))
            return ResponseEntity.status(403).build();

        LocalDateTime dateTime = LocalDateTime.parse(time.get("time"), SCHEDULE_TIME_FORMAT);

        Optional<Machine> machine = machineService.findOwnedById(Long.parseLong(id));

        if (machine.isPresent()) {
            Machine m = machine.get();
            machineService.scheduleMachineRestart(m.getId(), dateTime);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(404).build();
    }
}
