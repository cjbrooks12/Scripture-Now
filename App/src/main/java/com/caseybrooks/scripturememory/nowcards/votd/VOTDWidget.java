package com.caseybrooks.scripturememory.nowcards.votd;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class VOTDWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

        VOTDWidget myWidget = new VOTDWidget();
        myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //set pendingIntents on entire class of widgets
//        setWidgetClick(context, appWidgetManager);
//
//        VOTD votd = new VOTD(context);
//
//        //if verse is old, delete it from database (no need to keep it around, its not in any lists),
//        // and set currentVerse to null so that we download it again
//        if(votd.currentVerse != null) {
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
//            views.setTextViewText(R.id.widget_votd_reference, votd.currentVerse.getReference().toString());
//            views.setTextViewText(R.id.widget_votd_verse, votd.currentVerse.getText());
//
//            // Instruct the widget manager to update the widget
//            AppWidgetManager appWidgetManager2 = AppWidgetManager.getInstance(context);
//
//            int[] appWidgetIds2 = appWidgetManager2.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));
//
//            for (int i = 0; i < appWidgetIds2.length; i++) {
//                appWidgetManager2.updateAppWidget(appWidgetIds2[i], views);
//            }
//        }
    }

    public void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
//        Intent resultIntent = new Intent(context, MainActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
//        remoteViews.setOnClickPendingIntent(R.id.widget_votd_layout, resultPI);
//
//        ComponentName watchWidget = new ComponentName(context, VOTDWidget.class);
//        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}
