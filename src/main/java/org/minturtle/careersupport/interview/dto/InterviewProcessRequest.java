package org.minturtle.careersupport.interview.dto;

public class InterviewProcessRequest {

    private final String previousQuestion;
    private final String previousAnswer;

    public InterviewProcessRequest(String previousQuestion, String previousAnswer) {
        this.previousQuestion = previousQuestion;
        this.previousAnswer = previousAnswer;
    }


    public String getPreviousQuestion() {
        return previousQuestion;
    }

    public String getPreviousAnswer() {
        return previousAnswer;
    }
}
