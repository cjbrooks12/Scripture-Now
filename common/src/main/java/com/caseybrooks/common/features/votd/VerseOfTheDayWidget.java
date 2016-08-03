package com.caseybrooks.common.features.votd;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.common.R;

public class VerseOfTheDayWidget extends AppWidgetProvider implements OnResponseListener {

    VerseOfTheDayService service;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VerseOfTheDayWidget.class));

        VerseOfTheDayWidget myWidget = new VerseOfTheDayWidget();
        myWidget.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.context = context;
        service = new VerseOfTheDayService();
        service.run(this);
    }

    @Override
    public void responseFinished(boolean b) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.votd_widget);

        if(b) {
            views.setTextViewText(R.id.reference, service.getVerseOfTheDay().getPassage().getReference().toString());
            views.setTextViewText(R.id.verse, service.getVerseOfTheDay().getPassage().getText());
        }
        else {
            views.setTextViewText(R.id.reference, "Cannot load verse");
            views.setTextViewText(R.id.verse, "Check your internet connection and try again");
        }

        // Instruct the widget manager to update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, VerseOfTheDayWidget.class));

        for(int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
