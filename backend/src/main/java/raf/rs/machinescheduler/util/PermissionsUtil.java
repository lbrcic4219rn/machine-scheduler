package raf.rs.machinescheduler.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import raf.rs.machinescheduler.model.Permission;
import raf.rs.machinescheduler.model.enums.PermissionEnum;
import raf.rs.machinescheduler.services.UserService;

@Component
@RequiredArgsConstructor
public class PermissionsUtil {

    private final UserService userService;

    public boolean lacksPermission(PermissionEnum permissionEnum) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.userService.findByEmail(email)
                .map(user -> !isGranted(user.getPermission(), permissionEnum))
                .orElse(true);
    }

    private static boolean isGranted(Permission permission, PermissionEnum permissionEnum) {
        int flag = switch (permissionEnum) {
            case CAN_READ_USER -> permission.getCanReadUser();
            case CAN_CREATE_USER -> permission.getCanCreateUser();
            case CAN_DELETE_USER -> permission.getCanDeleteUser();
            case CAN_UPDATE_USER -> permission.getCanUpdateUser();
            case CAN_CREATE_MACHINE -> permission.getCanCreateMachine();
            case CAN_DESTROY_MACHINE -> permission.getCanDestroyMachine();
            case CAN_RESTART_MACHINE -> permission.getCanRestartMachine();
            case CAN_SEARCH_MACHINE -> permission.getCanSearchMachine();
            case CAN_START_MACHINE -> permission.getCanStartMachine();
            case CAN_STOP_MACHINE -> permission.getCanStopMachine();
        };
        return flag == 1;
    }
}
