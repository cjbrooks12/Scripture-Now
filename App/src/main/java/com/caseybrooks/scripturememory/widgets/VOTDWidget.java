package com.caseybrooks.scripturememory.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.Util;

public class VOTDWidget extends AppWidgetProvider {
    public static final String REFRESH_ALL = ".VOTDWidget.REFRESH_ALL";

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equals(REFRESH_ALL)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

            VOTDWidget myWidget = new VOTDWidget();
            myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;

        //set pendingIntents on entire class of widgets
        setWidgetClick(context, appWidgetManager);

        //if(cacheIsGood) return cachedVerse;
        //else if(connected) return downloadedVerse;
        //else return errorMessageVerse;

        //get new verse if connected, else just don't update widgets
        if(Util.isConnected(context)) {
//            new VOTDGetTask(context, MetaSettings.getBibleVersion(context), new OnTaskCompletedListener() {
//                @Override
//                public void onTaskCompleted(Object param) {
//                    if (param != null) {
//                        Passage passage = (Passage) param;
//                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
//                        views.setTextViewText(R.id.widget_votd_reference, passage.getReference().toString());
//                        views.setTextViewText(R.id.widget_votd_verse, passage.getText());
//
//                        // Instruct the widget manager to update the widget
//                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//
//                        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));
//
//                        for (int i = 0; i < appWidgetIds.length; i++) {
//                            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
//                        }
//                    }
//                }
//            }).execute();
        }
    }

    private void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
        remoteViews.setOnClickPendingIntent(R.id.widget_votd_layout, resultPI);

        ComponentName watchWidget = new ComponentName(context, VOTDWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}


