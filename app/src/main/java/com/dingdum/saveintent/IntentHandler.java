package com.dingdum.saveintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jinsuk.oh on 2016-09-29.
 */
public interface IntentHandler {
    public String getNameOfIntent();
    public void onHandle(Context context, Intent intent);
}