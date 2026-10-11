package raf.rs.machinescheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import raf.rs.machinescheduler.model.enums.Status;

import java.time.Instant;

@Data
@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy")
    @JsonIgnore
    private User user;

    private boolean active;

    private Instant createdAt;

    private boolean operationActive;

    @Version
    @JsonIgnore
    private Long version;
}
