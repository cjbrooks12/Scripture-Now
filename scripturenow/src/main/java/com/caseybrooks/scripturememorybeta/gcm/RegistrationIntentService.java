package com.caseybrooks.scripturememorybeta.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.caseybrooks.scripturememorybeta.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);
        }
        catch(Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean("sentTokenToServer", false).apply();
        }
    }

    private void sendRegistrationToServer(String token) {
//        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("sentTokenToServer", false)) {
//            try {
//                Log.i("sendRegToServer", "about to send");
//                URL url = new URL("https://heroku-website-cjbrooks12.c9.io/push/register");
//                Map<String, Object> params = new LinkedHashMap<>();
//                params.put("appID", "scripturenow");
//                params.put("deviceID", token);
//
//                StringBuilder postData = new StringBuilder();
//                for(Map.Entry<String, Object> param : params.entrySet()) {
//                    if(postData.length() != 0)
//                        postData.append('&');
//                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
//                    postData.append('=');
//                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
//                }
//                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//                conn.setDoOutput(true);
//                conn.getOutputStream().write(postDataBytes);
//
//                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//                for(int c = in.read(); c != -1; c = in.read())
//                    System.out.print((char) c);
//                Log.i("sendRegToServer", "done sending");
//                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("sentTokenToServer", true).apply();
//            }
//            catch(Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}

