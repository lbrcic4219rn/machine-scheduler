package raf.rs.machinescheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String email;

    private String name;
    private String surname;
    @JsonIgnore
    private String password;

    @Embedded
    @AttributeOverride(name = "can_create_user", column = @Column(name = "can_create_user"))
    @AttributeOverride(name = "can_read_user", column = @Column(name = "can_read_user"))
    @AttributeOverride(name = "can_update_user", column = @Column(name = "can_update_user"))
    @AttributeOverride(name = "can_delete_user", column = @Column(name = "can_delete_user"))
    @AttributeOverride(name = "can_search_machine", column = @Column(name = "can_search_machine"))
    @AttributeOverride(name = "can_start_machine", column = @Column(name = "can_start_machine"))
    @AttributeOverride(name = "can_stop_machine", column = @Column(name = "can_stop_machine"))
    @AttributeOverride(name = "can_restart_machine", column = @Column(name = "can_restart_machine"))
    @AttributeOverride(name = "can_create_machine", column = @Column(name = "can_create_machine"))
    @AttributeOverride(name = "can_destroy_machine", column = @Column(name = "can_destroy_machine"))
    private Permission permission;

    public User() {

    }

    public User(String email, String name, String surname, String password, Permission permission) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.permission = permission;
    }
}
