package com.royalclassico.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Single-document model for the next fixture banner.
 * Keeps the same fields as Fixture but stored in its own collection `next_fixture`.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "next_fixture")
public class NextFixture {
    @Id
    private String id;

    private String rivalTeam;
    private String date;
    private String time;
    private String stadium;
    private String result = "vs";
    private boolean isFinished = false;
    private String goalScorers;
}

