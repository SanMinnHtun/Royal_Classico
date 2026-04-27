package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Represents a player in the squad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "players")
public class Player {

    @Id
    private String id;

    @NotBlank(message = "Player name is required")
    private String name;

    @NotNull(message = "Position is required")
    private Position position;

    @NotNull(message = "Jersey number is required")
    @Min(value = 1, message = "Jersey number must be at least 1")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    private Integer jerseyNumber;

    /** Relative path under /uploads/, e.g. "players/image.jpg" */
    private String imagePath;

    /** Allowed positions for squad management */
    public enum Position {
        GK, DEF, MID, FWD
    }
}
