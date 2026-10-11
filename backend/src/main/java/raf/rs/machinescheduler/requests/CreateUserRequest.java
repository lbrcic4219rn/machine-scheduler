package raf.rs.machinescheduler.requests;

import lombok.Data;
import raf.rs.machinescheduler.model.Permission;

@Data
public class CreateUserRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private Permission permission;
}
