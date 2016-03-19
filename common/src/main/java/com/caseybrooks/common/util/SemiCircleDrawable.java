package com.caseybrooks.common.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class SemiCircleDrawable extends Drawable {

    Paint paint;
    RectF rectF;
    int color;
    int startAngle;
    int endAngle;

    public SemiCircleDrawable(int color, int startAngle, int endAngle) {
        this.color = color;
        rectF = new RectF();
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {

        Rect bounds = getBounds();
        rectF.set(bounds);

        canvas.save();
        canvas.translate(-1 * (bounds.left), 0);
        canvas.scale(2, 1);
        canvas.drawArc(rectF, startAngle, endAngle, true, paint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
