package edu.mit.ll.hadr.psiapanalytictester.task;

import android.os.AsyncTask;
import android.util.Log;

import com.github.devicehive.client.model.DHResponse;
import com.github.devicehive.client.model.DeviceNotification;
import com.github.devicehive.client.service.Device;
import com.github.devicehive.client.service.DeviceCommand;
import com.github.devicehive.client.service.DeviceHive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.mit.ll.hadr.psiapanalytictester.analytics.AnalyticUtils;

public class DeviceHiveAsyncTask extends AsyncTask<String, Void, List<DHResponse<DeviceNotification>>>
{
    private static final String TAG = "DHRequestTask";
    private DeviceHive deviceHive = null;

    @Override
    protected List<DHResponse<DeviceNotification>> doInBackground(String... deviceIds)
    {
        List<DHResponse<DeviceNotification>> res = new ArrayList<>();

        if (deviceHive == null) {
            Log.e(TAG, "DH is NULL");
            return null;
        }


        for (String id : deviceIds)
        {
            try
            {
                Log.i(TAG, "Executing for id: " + id);
                deviceHive.enableDebug(true);

                DHResponse<Device> deviceResponse = deviceHive.getDevice(id);
                Log.i(TAG, "Got device response");
                if (deviceResponse.isSuccessful())
                {
                    Device device = deviceResponse.getData();
                    Log.i(TAG, "GOT DEVICE!");

                    DHResponse<DeviceCommand> response = device.sendCommand("TEST CMD", new ArrayList<>());
                    if (response.isSuccessful()) {
                        Log.i(TAG, "CMD SUCCESS");
                    } else {
                        Log.i(TAG, "CMD FAILURE");
                    }

                    for (int i = 0; i < 10000000; i++) {
//            test sending notification
                        DHResponse<DeviceNotification> rres = device.sendNotification("TEST NOTI",
                                Collections.singletonList(AnalyticUtils.createTestParam("Test Noti", "Test_" + i,
                                        AnalyticUtils.generateWearableFilename(device.getName(), "Mobile"))));
                        if (rres.isSuccessful()) {
                            Log.i(TAG, "NOTI SUCCESS");
//                            res.add(rres);
                        } else {
                            Log.i(TAG, "CMD FAILURE");
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Log.w(TAG, "Caught interrupt: " + ie.getMessage());
                        }
                    }
                } else {
                    Log.w(TAG, "Failed: " + deviceResponse.getFailureData().getMessage());
                }
            } catch (Exception e) {

            }
        }

        return res;
    }

    public void setDeviceHive(DeviceHive deviceHive) {
        this.deviceHive = deviceHive;
    }
}
