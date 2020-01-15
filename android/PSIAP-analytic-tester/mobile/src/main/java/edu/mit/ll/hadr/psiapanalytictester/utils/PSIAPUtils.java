package edu.mit.ll.hadr.psiapanalytictester.utils;

import com.github.devicehive.client.model.Parameter;
import com.google.gson.Gson;

public final class PSIAPUtils
{
    public static boolean checkString(String val) {
        if (val == null || val.isEmpty()) {
            return false;
        }

        return true;
    }

    public static Parameter toDeviceHiveParameter(String title, Object o) {
        return new Parameter(title, new Gson().toJson(o));
    }


}
