package edu.mit.ll.hadr.psiapanalytictester.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public final class DHUtils
{
    private static final String ACTION = "action";
    private static final String REQUEST_ID = "requestId";
    private static final String TOKEN = "token";
    private static final String STATUS = "status";
    private static final String UUID = "uuid";

    private static final String AUTHENTICATE_ACTION = "authenticate";


    private static JSONObject createUniqueRequestId() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(UUID, java.util.UUID.randomUUID());

        return json;
    }

    public static JSONObject createAuthenticateMessage(String accessToken) throws JSONException
    {
        return buildDhWsMessage(AUTHENTICATE_ACTION, accessToken);
    }

    public static JSONObject buildDhWsMessage(String action, String accessToken)
            throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("action", action);
        json.put("requestId", createUniqueRequestId());
        json.put(TOKEN, accessToken);

        return json;
    }
}
