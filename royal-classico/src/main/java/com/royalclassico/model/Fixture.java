package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Multi-entry model for fixtures. Each save() creates a new document (unique id).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fixtures")
public class Fixture {

    @Id
    private String id;

    /** The rival team name */
    private String rivalTeam;

    /** Match date as a plain string, e.g. "2026-06-15" */
    private String date;

    /** Kick-off time as a plain string, e.g. "15:00" */
    private String time;

    /** Stadium / venue name */
    private String stadium;

    /** Result display string — defaults to "vs" before the match. */
    private String result = "vs";

    /** True once the match has been played and a result is recorded */
    private boolean isFinished = false;

    /** Optional: goal scorer names and times */
    private String goalScorers;

    /** Optional image for the fixture (cloud URL when uploaded) */
    private String imagePath;

    /** ImageKit file identifier (remote) — used for deletion from the cloud */
    private String imageFileId;
}
