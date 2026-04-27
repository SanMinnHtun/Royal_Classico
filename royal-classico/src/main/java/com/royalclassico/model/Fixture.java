package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Single-entry model for the next upcoming match fixture.
 * Only one document of this type should exist in the DB (upsert pattern).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "next_fixture")
public class Fixture {

    @Id
    private String id;

    @NotBlank(message = "Opponent name is required")
    private String opponent;

    @NotNull(message = "Match date is required")
    private LocalDate matchDate;

    @NotNull(message = "Match time is required")
    private LocalTime matchTime;

    @NotBlank(message = "Pitch/venue is required")
    private String pitch;
}
