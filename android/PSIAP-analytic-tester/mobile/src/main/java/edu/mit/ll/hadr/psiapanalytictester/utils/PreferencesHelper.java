/*
 *
 *
 *   PreferencesHelper.java
 *
 *   Copyright (C) 2017 DataArt
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package edu.mit.ll.hadr.psiapanalytictester.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesHelper
{
    private static final String DEFAULT_PREFERENCES = "default_preferences";
    private SharedPreferences sharedPreferences;

    public static final String SERVER_URL = "pref_serverhost";
    public static final String ACCESS_TOKEN = "pref_dh_access_token";
    public static final String REFRESH_TOKEN = "pref_dh_refresh_token";
    public static final String DEVICE_ID = "pref_dh_deviceid";
    public static final String RTSP_URL = "pref_rtspurl";

    private static final String DEVICE_ID_FORMAT = "ANDROID-EXAMPLE-%s";

    private PreferencesHelper() {
    }

    public static PreferencesHelper getInstance() {
        return PreferencesHelper.InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        static final PreferencesHelper INSTANCE = new PreferencesHelper();
    }

    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }

    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences(DEFAULT_PREFERENCES, MODE_PRIVATE);
    }

    public void putRefreshToken(String refreshToken) {
        sharedPreferences.edit().putString(REFRESH_TOKEN, refreshToken).apply();
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN, "");
    }

    public void putServerUrl(String url) {
        sharedPreferences.edit().putString(SERVER_URL, url).apply();
    }

    public String getServerUrl() {
        return sharedPreferences.getString(SERVER_URL, "");

    }

    public void putDeviceId(String deviceId) {
        sharedPreferences.edit().putString(DEVICE_ID, deviceId).apply();
    }

    public String getDeviceId() {
        return sharedPreferences.getString(DEVICE_ID, "");
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN, "");
    }

    public void putAccessToken(String token) {
        sharedPreferences.edit().putString(ACCESS_TOKEN, token).apply();
    }

    public void putRtspUrl(String rtspUrl) {
        sharedPreferences.edit().putString(RTSP_URL, rtspUrl).apply();
    }

    public String getRtspUrl() {
        return sharedPreferences.getString(RTSP_URL, "");
    }

    public String getDeviceIdFormat() {
        return DEVICE_ID_FORMAT;
    }
}
