package raf.rs.machinescheduler.responses;

import lombok.Data;
import raf.rs.machinescheduler.model.Permission;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private Permission permission;

    public UserResponse(Long id, String name, String surname, String email, Permission permission) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.permission = permission;
    }

    public UserResponse(String name, String surname, String email, Permission permission) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.permission = permission;
    }
}
