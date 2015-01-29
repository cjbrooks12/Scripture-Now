package com.caseybrooks.scripturememory.nowcards.main;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class MainWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        //Force update of all widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MainWidget.class));

        MainWidget myWidget = new MainWidget();
        myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //set pendingIntents on entire class of widgets
        setNextButtonClick(context, appWidgetManager);
        setWidgetClick(context, appWidgetManager);

        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            configureKeyguardWidget(context, appWidgetManager, appWidgetIds[i]);
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    private void configureKeyguardWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews nextButton = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);

            Bundle myOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
            boolean isKeyguard = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

            if (isKeyguard) {
                nextButton.setViewVisibility(R.id.widget_votd_refresh, View.GONE);
                appWidgetManager.updateAppWidget(appWidgetId, nextButton);
            }
        }
    }

    private void setNextButtonClick(Context context, AppWidgetManager appWidgetManager) {
        Intent nextIntent = new Intent(context, MainVerse.NextVerseReceiver.class);
        PendingIntent nextPI = PendingIntent.getBroadcast(context, 0, nextIntent, 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
        remoteViews.setOnClickPendingIntent(R.id.widget_votd_refresh, nextPI);

        ComponentName watchWidget = new ComponentName(context, MainWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    private void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
        remoteViews.setOnClickPendingIntent(R.id.widget_main_layout, resultPI);

        ComponentName watchWidget = new ComponentName(context, MainWidget.class);
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        int id = MainVerse.getVerseId(context);

        MainVerse mv = new MainVerse(context);
        if (mv.passage != null) {
            mv.setPassageFormatted();

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
            views.setTextViewText(R.id.widget_main_reference, mv.passage.getReference().toString());
            views.setTextViewText(R.id.widget_main_verse, mv.passage.getText());

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main_verse);
            views.setTextViewText(R.id.widget_main_reference, "No verse set!");
            views.setTextViewText(R.id.widget_main_verse, "Why don't you try adding some more verses, or start memorizing a different list?");

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
