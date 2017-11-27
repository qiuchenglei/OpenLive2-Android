package io.agora.utils;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class CommonFunc {
    /**
     * Motion & Key Event detected
     */
    private static long mLastTouchTime = -1;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static final boolean checkDoubleTouchEvent(MotionEvent event, View view) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mLastTouchTime == -1 || (SystemClock.elapsedRealtime() - mLastTouchTime) >= 500) {
                mLastTouchTime = SystemClock.elapsedRealtime();
            } else {
                return true;
            }
        }
        return false;
    }

    public static final boolean checkDoubleKeyEvent(KeyEvent event, View view) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            if (mLastTouchTime != -1 && (SystemClock.elapsedRealtime() - mLastTouchTime) < 500) {
                return true;
            }
            mLastTouchTime = SystemClock.elapsedRealtime();
        }

        return false;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
