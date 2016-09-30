package com.dingdum.saveintent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jinsuk.oh on 2016-09-29.
 */
public class SaveIntentReceiver extends BroadcastReceiver {
    private static final String TAG = SaveIntentReceiver.class.getSimpleName();
    private static final String SHARED_PREFERENCES_KEY = "com.dingdum.saveintent.prefs";
    private static final String SAVE_INTENT_LIST = "save_intent_list";
    private static final Object sLock = new Object();
    private static SaveIntentReceiver sSaveIntentReceiver = null;
    private static boolean sUseQueue = true;
    private static HashMap<String, IntentHandler> sIntents = new HashMap<String, IntentHandler>();

    private SaveIntentReceiver() {
        sIntents.clear();
        Class<?>[] declaredClass = IntentHandlerImpl.class.getDeclaredClasses();
        for (Class<?> checkclass : declaredClass) {
            try {
                Object obj = checkclass.newInstance();
                if (obj instanceof IntentHandler) {
                    sIntents.put(((IntentHandler)obj).getNameOfIntent(), (IntentHandler)obj);
                }
            } catch (InstantiationException e) {
                Log.w(TAG, "not found");
            } catch (IllegalAccessException e){
                Log.w(TAG, "not found");
            }
        }
    }
    private static BroadcastReceiver getInstance() {
        if (sSaveIntentReceiver == null) {
            sSaveIntentReceiver = new SaveIntentReceiver();
        }
        return sSaveIntentReceiver;
    }

    public static void registerReceiver(Context context) {
        context.registerReceiver(getInstance(), getIntentFilter());
    }

    private static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        Set<String> intentActions = sIntents.keySet();
        for (String action : intentActions) {
            intentFilter.addAction(action);
        }
        return intentFilter;
    }

    public static void unregisterReceiver(Context context) {
        try {
            context.unregisterReceiver(getInstance());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error on unregisterReceiver : " + e.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive intent: " + intent);
        addQueue(context, intent);
    }

    private static void addQueue(Context context, Intent intent) {
        SharedPreferences sp = getSharedPreferences(context);
        addToQueue(sp, intent);
        if (!isUseQueue()) {
            flushQueue(context);
        }
    }

    private static boolean isUseQueue() {
        return sUseQueue;
    }

    public static void enableQueue() {
        sUseQueue = true;
    }

    public static void disableAndFlushQueue(Context context) {
        sUseQueue = false;
        flushQueue(context);
    }

    private static void addToQueue(SharedPreferences sharedPrefs, Intent intent) {
        synchronized (sLock) {
            String uriString = intent.toUri(0);
            if (uriString != null) {
                Set<String> strings = sharedPrefs.getStringSet(SAVE_INTENT_LIST, null);
                if (strings == null) {
                    strings = new HashSet<String>(1);
                } else {
                    strings = new HashSet<String>(strings);
                }
                strings.add(uriString);
                sharedPrefs.edit().putStringSet(SAVE_INTENT_LIST, strings).commit();
            }
        }
    }

    private static void flushQueue(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        ArrayList<Intent> intentQueue = getAndClearIntentQueue(sp, context);
        if (!intentQueue.isEmpty()) {
            Iterator<Intent> iter = intentQueue.iterator();

            while (iter.hasNext()) {
                final Intent pendingIntent = iter.next();

                if (sIntents.containsKey(pendingIntent.getAction())) {
                    sIntents.get(pendingIntent.getAction()).onHandle(context, pendingIntent);
                }
            }
        }
    }

    private static ArrayList<Intent> getAndClearIntentQueue(
            SharedPreferences sharedPrefs, Context context) {
        synchronized (sLock) {
            Set<String> strings = sharedPrefs.getStringSet(SAVE_INTENT_LIST, null);
            Log.d(TAG, "Getting and clearing PENDING_INTENT_LIST: " + strings);

            if (strings == null) {
                return new ArrayList<Intent>();
            }
            ArrayList<Intent> intents = new ArrayList<Intent>();
            for (String intentString : strings) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(intentString, 0);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (intent != null) {
                    intents.add(intent);
                }
            }

            sharedPrefs.edit()
                    .putStringSet(SAVE_INTENT_LIST, new HashSet<String>())
                    .commit();
            return intents;
        }
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }
}
