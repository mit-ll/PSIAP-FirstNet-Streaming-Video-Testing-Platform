package edu.mit.ll.hadr.psiapanalytictester.analytics.model.iva;

import java.util.Date;

import edu.mit.ll.hadr.psiapanalytictester.analytics.model.FramePattern;

public class IVAModel
{
    private boolean day;
    private boolean isInVehicle;
    private Date ts; // timestamp
    private String frameId;
    private FramePattern framePattern;

    private String daynightModelUsed;
    private String modelUsed;

    public IVAModel(boolean isDay, boolean isInVehicle, String frameId,
                    FramePattern pattern, String daynightModelUsed, String modelUsed)
    {
        this.day = isDay;
        this.isInVehicle = isInVehicle;
        this.frameId = frameId;
        this.framePattern = pattern;
        this.daynightModelUsed = daynightModelUsed;
        this.modelUsed = modelUsed;
        this.ts = new Date();
    }

    public boolean isDay() {
        return day;
    }

    public boolean isInVehicle() {
        return isInVehicle;
    }

    public Date getTs() {
        return ts;
    }

    public String getFrameId() {
        return frameId;
    }

    public FramePattern getFramePattern() {
        return framePattern;
    }

    public String getDaynightModelUsed() {
        return daynightModelUsed;
    }

    public String getModelUsed() {
        return modelUsed;
    }
}
