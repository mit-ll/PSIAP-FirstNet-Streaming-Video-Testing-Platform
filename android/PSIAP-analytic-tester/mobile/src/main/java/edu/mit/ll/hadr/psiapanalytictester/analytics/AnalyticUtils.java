package edu.mit.ll.hadr.psiapanalytictester.analytics;

import com.github.devicehive.client.model.Parameter;
import com.github.devicehive.client.service.DeviceHive;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.mit.ll.hadr.psiapanalytictester.analytics.model.FramePattern;
import edu.mit.ll.hadr.psiapanalytictester.dhmodel.IVAParam;
import edu.mit.ll.hadr.psiapanalytictester.dhmodel.StartTestParam;

public final class AnalyticUtils
{
    public static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("YYYY-MM-dd");

    public static String getDate() { return String.valueOf(DATEFORMAT.format(new Date())); }
    public static String getTimestamp() { return String.valueOf(new Date().getTime()); }

    public static String generateWearableFilename(String deviceName, String source)
    {
        return new StringBuffer().append(deviceName.replaceAll(" ", "_"))
                .append("_").append(source).append("_")
                .append(getDate()).append("_").append(getTimestamp()).toString();
    }

    public static DeviceHive createDeviceHiveIfNotExists(String serverUrl,
                                                   String refreshToken, String accessToken)
    {
        return DeviceHive.getInstance().init(serverUrl,
                    refreshToken.trim(), accessToken);

    }

    public static StartTestParam genStartTestParam(String title, String message) {
        StartTestParam p = new StartTestParam();
        p.setTitle(title);
        p.setMessage(message);

        return p;
    }

    public static IVAParam genAnalyticParam(String frameId) {
        IVAParam p = new IVAParam();
        p.setFrameId(frameId);
        p.setDay(true);
        p.setInvehicle(true);

        return p;
    }

    public static Parameter createIVAParams(String frame) {
        IVAParam p = genAnalyticParam(frame);
        Gson gson = new Gson();
        return new Parameter("IVA Frame", gson.toJson(p));
    }

    public static Parameter createTestParam(String title, String name, String frame) {
        StartTestParam p = genStartTestParam(name, frame);
        Gson gson = new Gson();
        return new Parameter(title, gson.toJson(p));
    }


    public static FramePattern parseFramePattern() {
        return FramePattern.WEBCAM_PATTERN;
    }
}
