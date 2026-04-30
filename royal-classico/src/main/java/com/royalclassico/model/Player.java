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

import java.util.List;

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

    // Legacy / public-facing short name used by some controllers
    private String name;

    @NotBlank(message = "Real name is required")
    private String realName;

    private String jerseyName;

    private Integer age;

    @NotNull(message = "Jersey number is required")
    @Min(value = 1, message = "Jersey number must be at least 1")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    private Integer jerseyNumber;

    /** Relative path under /uploads/, e.g. "players/image.jpg" */
    private String imagePath;

    /** Multiple allowed positions (GK, DEF, MID, FWD) as strings */
    private List<String> positions;

    /** Single canonical position (legacy code expects Player.Position) */
    private Position position;

    public enum Position {
        GK, DEF, MID, FWD
    }
}
