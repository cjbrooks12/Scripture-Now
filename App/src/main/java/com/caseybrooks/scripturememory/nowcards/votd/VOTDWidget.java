package com.caseybrooks.scripturememory.nowcards.votd;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class VOTDWidget extends AppWidgetProvider implements IVerseViewListener {

    VOTD votd;
    Context context;

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
        setWidgetClick(context, appWidgetManager);

        this.context = context;
        votd = new VOTD(context);
        votd.setListener(this);
        votd.loadTodaysVerse();
    }

    public void setWidgetClick(Context context, AppWidgetManager appWidgetManager) {
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

    @Override
    public boolean onBibleLoaded(Bible bible, LoadState loadState) {
        return false;
    }

    @Override
    public boolean onVerseLoaded(final AbstractVerse abstractVerse, final LoadState loadState) {
        if(abstractVerse != null && loadState != LoadState.Failed) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_votd);
            views.setTextViewText(R.id.widget_votd_reference, abstractVerse.getReference().toString());
            views.setTextViewText(R.id.widget_votd_verse, abstractVerse.getText());

            // Instruct the widget manager to update the widget
            AppWidgetManager appWidgetManager2 = AppWidgetManager.getInstance(context);

            int[] appWidgetIds2 = appWidgetManager2.getAppWidgetIds(new ComponentName(context, VOTDWidget.class));

            for (int i = 0; i < appWidgetIds2.length; i++) {
                appWidgetManager2.updateAppWidget(appWidgetIds2[i], views);
            }
        }

        return true;
    }
}
