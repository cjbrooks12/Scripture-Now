package com.caseybrooks.common.util;

import android.content.DialogInterface;

public class CancelDialogAction implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
    }
}