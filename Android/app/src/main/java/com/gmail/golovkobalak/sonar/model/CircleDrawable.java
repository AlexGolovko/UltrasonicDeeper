package com.gmail.golovkobalak.sonar.model;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class CircleDrawable extends Drawable {

    private Paint paint;
    private int diameter;

    public CircleDrawable(int diameter, int color) {
        this.diameter = diameter;
        paint = new Paint();
        paint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicWidth() {
        return diameter;
    }

    @Override
    public int getIntrinsicHeight() {
        return diameter;
    }

    public static Drawable create(int diameter, int color) {
        Bitmap bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        CircleDrawable drawable = new CircleDrawable(diameter, color);
        drawable.draw(canvas);
        return new BitmapDrawable(bitmap);
    }
}