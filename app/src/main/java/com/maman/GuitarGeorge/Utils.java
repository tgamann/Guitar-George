package com.maman.GuitarGeorge;

import android.view.View;
import android.widget.ImageView;
import static com.maman.GuitarGeorge.Constants.*;

/*
 * Utility routines used across multiple classes.
 */
public class Utils {
    private static int mPreviousTab = 0;

    public static void setViewImage(View v, String newImageName) {
        ((ImageView)v).setImageResource(v.getResources().getIdentifier(newImageName,
                "drawable", PACKAGE_NAME));
        v.setTag(newImageName);
        // Invalidate the view to force a redraw
        v.invalidate();
    }

    public static void setPreviousTab(int tabPosition) {
        mPreviousTab = tabPosition;
    }
    public static int getPreviousTab() {
        return mPreviousTab;
    }

}
