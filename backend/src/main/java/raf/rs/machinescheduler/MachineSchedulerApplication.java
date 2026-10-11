package raf.rs.machinescheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MachineSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MachineSchedulerApplication.class, args);
    }

}
