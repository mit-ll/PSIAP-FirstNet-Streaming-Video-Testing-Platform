package edu.mit.ll.hadr.psiapanalytictester;

import com.github.devicehive.client.model.DHResponse;
import com.github.devicehive.client.model.DeviceFilter;
import com.github.devicehive.client.model.DeviceNotification;
import com.github.devicehive.client.model.FailureData;
import com.github.devicehive.client.model.Parameter;
import com.github.devicehive.client.service.Device;
import com.github.devicehive.client.service.DeviceCommand;
import com.github.devicehive.client.service.DeviceHive;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.ll.hadr.psiapanalytictester.dhmodel.IVAParam;

public class DhTest
{
    private DeviceHive deviceHive;
    private int count = 1;

    private void log (String s) {
        System.out.println(s);
    }

    private void createDeviceHiveIfNotExists(String serverUrl, String refreshToken, String accessToken) {

        if (deviceHive == null) {
            deviceHive = DeviceHive.getInstance().init(serverUrl,
                    refreshToken.trim(), accessToken);
        }
    }

    private IVAParam getAnalyticParam() {
        IVAParam p = new IVAParam();
        p.setFrameId("test" + String.valueOf(count++));
        p.setDay(true);
        p.setInvehicle(true);

        return p;
    }

    private Parameter createIVAParams() {
        IVAParam p = getAnalyticParam();
        Gson gson = new Gson();
        return new Parameter("IVA Frame", gson.toJson(p));
    }

    public void test() {
        final String serverUrl = "http://localhost:8088/";
        final String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjp7ImEiOlswXSwiZSI6MTU4MDQ0NjgwMDAwMCwidCI6MSwidSI6MywibiI6WyIqIl0sImR0IjpbIioiXX19.gNj2-hhJa25VwrozC-RpvuGfJGu5JvQq-x0U3jml17M";
        final String refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJwYXlsb2FkIjp7ImEiOlswXSwiZSI6MTU4MDQ0NjgwMDAwMCwidCI6MCwidSI6MywibiI6WyIqIl0sImR0IjpbIioiXX19.sX2EcyawakLB0A7XuQYp1E9A0dsct96s924Tn260CyA";
        final String deviceId = "LSLPMgvTB6GCmKwr6I2d1OkBuKwSM6TpJQCN";

        createDeviceHiveIfNotExists(serverUrl, refreshToken, authToken);

        deviceHive.enableDebug(false);

        DeviceFilter df = new DeviceFilter();
//        deviceHive.listDevices(df);
        DHResponse<List<Device>> res  = deviceHive.listDevices(df);
        if (res.isSuccessful()) {
            log("SUCCESS");
            for (Device d : res.getData()) {
                log("Device id: " + d.getId());
            }
        }

        DHResponse<Device> devicehiveResponse = deviceHive.getDevice(deviceId);
        if (devicehiveResponse.isSuccessful()) {
            Device device = devicehiveResponse.getData();
            log("Got DH Device!");

//            test sending command
            DHResponse<DeviceCommand> response = device.sendCommand("TEST CMD", new ArrayList<>());
            if (response.isSuccessful()) {
                log("CMD SUCCESS");
            } else {
                log("CMD FAILURE");
            }

//            test sending notification
            DHResponse<DeviceNotification> rres = device.sendNotification("TEST NOTI", Collections.singletonList(createIVAParams()));
            if (rres.isSuccessful()) {
                log("NOTI SUCCESS");
            } else {
                log("CMD FAILURE");
            }
        } else {
            FailureData failureData = devicehiveResponse.getFailureData();
            int code = failureData.getCode();
            String message = failureData.getMessage();
            log("Failed: " + message);
        }


    }

    public static void main(String[] args) {
        DhTest dt = new DhTest();
        dt.test();
    }
}
