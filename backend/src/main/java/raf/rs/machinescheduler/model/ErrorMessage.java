package raf.rs.machinescheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "error_message")
public class ErrorMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Instant errorDate;

    private Long machineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    private String operationName;
    private String message;

    public ErrorMessage(Instant errorDate, Long machineId, User user, String operationName, String message) {
        this.errorDate = errorDate;
        this.machineId = machineId;
        this.user = user;
        this.operationName = operationName;
        this.message = message;
    }
}
