package com.example.securelti;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingViewService extends Service implements View.OnClickListener {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private TextView url;
    private String stringToBePassedInView;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        expandedView = mFloatingView.findViewById(R.id.layoutExpanded);
        url = mFloatingView.findViewById(R.id.url);

        mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);
        expandedView.setOnClickListener(this);

        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        collapsedView.setVisibility(View.GONE);
                        expandedView.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        // Retrieve the URL from the intent if available
        Intent intent = new Intent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                stringToBePassedInView = extras.getString("url");
                Log.d("FloatingService", "onCreate stringToBePassedInView: " + stringToBePassedInView);
                urlSetter();
            }
        }
    }

    public void urlSetter() {
        Log.d("FloatingService", "urlSetter stringToBePassedInView: " + stringToBePassedInView);
        Log.d("FloatingService", "now setting the URL of TextView");
        url.setText(stringToBePassedInView);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            stringToBePassedInView = extras.getString("url");
            Log.d("FloatingService", "onStartCommand stringToBePassedInView: " + stringToBePassedInView);
            urlSetter();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layoutExpanded) {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.GONE);
        } else if (v.getId() == R.id.buttonClose) {
            stopSelf();
        }
    }
}
