package edu.mit.ll.hadr.psiapanalytictester.dhmodel;

public class IVAParam {
    private String frameId;
    private String message;
//    private Date date;
    private boolean isDay;
    private boolean isInvehicle;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDay() {
        return isDay;
    }

    public void setDay(boolean day) {
        isDay = day;
    }

    public boolean isInvehicle() {
        return isInvehicle;
    }

    public void setInvehicle(boolean invehicle) {
        isInvehicle = invehicle;
    }
}
