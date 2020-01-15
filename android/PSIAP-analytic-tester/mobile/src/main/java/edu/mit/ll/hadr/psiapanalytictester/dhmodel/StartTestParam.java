package edu.mit.ll.hadr.psiapanalytictester.dhmodel;

import java.util.Date;

public class StartTestParam
{
    private String title;
    private final Long created;
    private String message;

    public StartTestParam() {
        created = new Date().getTime();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreated() {
        return new Date().getTime();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
