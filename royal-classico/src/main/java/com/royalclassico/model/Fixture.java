package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Single-entry model for the next upcoming match fixture.
 * Fields use simple types (String for date/time) to avoid serialization issues.
 * Only one document of this type should exist in the DB (upsert pattern).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "next_fixture")
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

    /**
     * Result display string — defaults to "vs" before the match,
     * updated to "2-1" or similar after.
     */
    private String result = "vs";

    /** True once the match has been played and a result is recorded */
    private boolean isFinished = false;
}
