package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a player in the squad.
 * Simplified model — positions are stored as plain strings (GK, DEF, MID, FWD).
 * No validation annotations to prevent crash-on-save when fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "players")
public class Player {

    @Id
    private String id;

    /** Display name (also used as the public-facing name) */
    private String name;

    /** Short name printed on the jersey */
    private String jerseyName;

    private Integer age;

    private Integer jerseyNumber;

    /** Relative web path, e.g. "/uploads/players/image.jpg" */
    private String imagePath;

    /** Multiple allowed positions as plain strings: GK, DEF, MID, FWD */
    private List<String> positions;

    /** Tactical role (e.g. "Playmaker", "Wing-Back") — optional */
    private String tacticalRole;
}
