package com.dingdum.saveintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jinsuk.oh on 2016-09-29.
 */
public class IntentHandlerImpl {
    public static class ChangeBackgroundColorHandler implements IntentHandler {
        private static final String TAG = ChangeBackgroundColorHandler.class.getSimpleName();
        private static final String CHANGE_BACKGROUND_COLOR_INTENT = "com.dingdum.action.CHANGE_BACKGOUND_COLOR";
        private static final String BACKGROUND_COLOR = "background_color";

        @Override
        public String getNameOfIntent() {
            return CHANGE_BACKGROUND_COLOR_INTENT;
        }
        @Override
        public void onHandle(Context context, Intent intent) {
            context.removeStickyBroadcast(intent);
            int color = (int)intent.getLongExtra(BACKGROUND_COLOR, 0);
            Log.d(TAG, "onHandle intent:" + intent + " color: " + color);

            if(context instanceof  Activity){
                ((Activity)context).findViewById(R.id.content_layout).setBackgroundColor(color);
            }

        }
    };
}
