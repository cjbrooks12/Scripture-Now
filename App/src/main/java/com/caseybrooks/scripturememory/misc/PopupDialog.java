package com.caseybrooks.scripturememory.misc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class PopupDialog extends AlertDialog {

    int maxProgress;
    int progress;

    protected PopupDialog(Context context) {
        super(context);
    }

    public void setProgressMax(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setProgressIndeterminate(boolean indeterminate) {

    }

    public void startProgress() {

    }

    public void endProgress() {

    }

    public void setTitle(String title) {

    }

    public void setDescription(String description) {

    }

    public void setPositiveButton(String text, DialogInterface.OnClickListener listener) {

    }

    public void setNegativeButton(String text, DialogInterface.OnClickListener listener) {

    }

    public void setNeutralButton(String text, DialogInterface.OnClickListener listener) {

    }

    public void setNeutralButton1(String text, DialogInterface.OnClickListener listener) {

    }

    public void setNeutralButton2(String text, DialogInterface.OnClickListener listener) {

    }
}
