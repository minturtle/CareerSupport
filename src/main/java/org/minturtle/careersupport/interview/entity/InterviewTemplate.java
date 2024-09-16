package org.minturtle.careersupport.interview.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "interview_templates")
public class InterviewTemplate {

    private String userId;
    private String theme;

}
