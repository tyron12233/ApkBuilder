package com.tyron.ide.util;

import android.util.TypedValue;
import android.widget.Toast;

import static com.apk.builder.ApplicationLoader.getContext;

public class AndroidUtilities {

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, getContext().getResources().getDisplayMetrics());
        return (int) Math.ceil(dip);
    }
    
    public static void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}