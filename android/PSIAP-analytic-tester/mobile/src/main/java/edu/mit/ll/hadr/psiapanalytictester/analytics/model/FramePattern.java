package edu.mit.ll.hadr.psiapanalytictester.analytics.model;

public enum FramePattern {
    CAMERA_PATTERN("wearable"),
    FILE_PATTERN("file"),
    WEBCAM_PATTERN("webcam");

    private String method;

    private FramePattern(String method) { this.method = method; }

    public FramePattern getCameraPattern() { return CAMERA_PATTERN; }

    public FramePattern getWebcamPattern() { return WEBCAM_PATTERN; }
}
