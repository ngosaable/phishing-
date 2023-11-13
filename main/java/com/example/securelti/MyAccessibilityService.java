package com.example.securelti;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "RPADML_AccessServ";
    private String currentURL = "";
    private boolean networkInit;
    private RequestQueue queue; // Volley RequestQueue

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (networkInit) {
            // already network request queue initialized
        } else {
            // init network request queue
            queue = Volley.newRequestQueue(this);
            networkInit = true;
        }

        AccessibilityNodeInfo source = event.getSource();

        if (source == null)
            return;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            // Handle the event for window content changes here
            // You can check for the package name and extract the URL as needed.
            final String packageName = String.valueOf(source.getPackageName());
            String BROWSER_LIST = "com.android.chrome, com.UCMobile.intl, org.mozilla.firefox, com.instagram.android, com.facebook.katana";

            // Check if the packageName is in your browser list
            if (BROWSER_LIST.contains(packageName)) {
                try {
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    getUrlsFromViews(nodeInfo);
                } catch (StackOverflowError ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void getUrlsFromViews(AccessibilityNodeInfo info) {
        // Loop through views and extract URLs as needed
        // Modify this function to capture and process URLs
        // For example, if a URL is found, you can check it and send it for checking.
        try {
            if (info == null)
                return;

            if (info.getText() != null && info.getText().length() > 0) {
                String capturedText = info.getText().toString();

                if (capturedText.contains("https://") || capturedText.contains("http://") || capturedText.contains("www.")) {
                    if (!currentURL.equals(capturedText)) {
                        // Do something with the URL.
                        currentURL = capturedText;
                        Log.d(TAG, "Found URL: " + capturedText);
                        checkURL(currentURL);
                    }
                }
            }

            for (int i = 0; i < info.getChildCount(); i++) {
                AccessibilityNodeInfo child = info.getChild(i);
                getUrlsFromViews(child);
                if (child != null) {
                    child.recycle();
                }
            }
        } catch (StackOverflowError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkURL(String url) {
        // This is where you can process the URL or send it for checking.
        // Example: Send the URL for checking and handle the response
        sendURLForChecking(url);
    }

    private void sendURLForChecking(String url) {
        // Implement the logic to send the URL to the specified endpoint for checking.
        // You can use a library like Volley for making the HTTP request.
        // Handle the response and check if it's true or false.

        // Example using Volley:
        String checkLinkURL = "http://192.168.137.138:3000/check-link"; // Replace with your endpoint URL

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("url", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, checkLinkURL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Handle the response (true or false)
                            boolean isPhishing = response.getBoolean("isPhishing");
                            handlePhishingResponse(isPhishing);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error (e.g., network error or server error)
                        Log.e(TAG, "Error while checking URL: " + error.getMessage());
                    }
                });

        // Add the request to your RequestQueue to execute the HTTP request.
        queue.add(request);
    }

    private void handlePhishingResponse(boolean isPhishing) {
        // Handle the phishing response here, e.g., show a message or take appropriate action
        if (isPhishing) {
            // It's a phishing site, show floating window and request the "System Alert Window" permission
            showFloatingWindow(currentURL);
            askPermission();
            Log.d(TAG, "You need System Alert Window Permission to do this");
        } else {
            // It's not a phishing site, continue with your application logic
            // You may want to provide feedback to the user or perform other actions
            Log.d(TAG, "The site is not a phishing site");
        }
    }

    private void showFloatingWindow(String urlToShowInView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            // Show the FloatingViewService
            Intent intent = new Intent(this, FloatingViewService.class);
            intent.putExtra("url", urlToShowInView);
            startService(intent);
        }
    }

    private void askPermission() {
        // Implement the logic to request the "System Alert Window" permission here.
        // You can show a dialog or open the system settings to allow the permission.
        // Be sure to handle the permission result.

        // Example:
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        // Handle service interruption if needed.
    }
}
