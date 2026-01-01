package com.example.solution.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_events")
@Data
@Getter
@Setter
public class ProcessedEvent{
    /**
     * Logs unique eventId
     */
    @Id
    private String eventId; //Unique Id from Stripe/Paypal
    private LocalDateTime receivedAt;

}
