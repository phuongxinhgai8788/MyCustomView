package com.example.mycustomview;

import static java.lang.Math.PI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomView extends View {

    private OnTimeChange onTimeChange;

    interface OnTimeChange {
        void onWakeupTimeChange(float wakeupTime);

        void onSleepTimeChange(float sleepTime);
    }

    private final Bitmap bitmap =
            BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.oclock
            );
    private Paint backgroundPaint;
    private float gapCircleBetweenLine;
    private float strokeWidth;
    private float radius;
    private float cx, cy;
    private float c = 0;


    private Paint arcPaint;
    private RectF arcRect;


    private Paint sleepPaint;
    private float sleepRadian;
    private boolean isSleepButton;
    private float prevSleepX;
    private float prevSleepRadian;
    private float cOfSleep = 0;
    private float sleepTime;

    private Paint wakePaint;
    private float wakeupRadian;
    private boolean isWakeupButton;
    private float prevWakeupX;
    private float prevWakeupRadian;
    private float cOfWakeup = 0;
    private float wakeTime;

    private Paint periodPaint;
    private float period;

    private boolean isInit;

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    private void initial() {

        strokeWidth =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        25,
                        getContext().getResources().getDisplayMetrics()
                );


        gapCircleBetweenLine =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        getContext().getResources().getDisplayMetrics()
                );


        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);


        arcPaint = new Paint();
        arcPaint.setColor(Color.YELLOW);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokeWidth);

        sleepPaint = new Paint();
        sleepPaint.setColor(Color.RED);

        wakePaint = new Paint();
        wakePaint.setColor(Color.GREEN);

        periodPaint = new Paint();
        periodPaint.setColor(Color.BLACK);
        periodPaint.setTextSize(40);


    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width, height;
        width = height = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(width, height);

        if (!isInit) {
            cx = cy = width / 2f;
            radius = (cx - gapCircleBetweenLine - (strokeWidth / 2));
            c = (float) (radius * 2 * PI);
            prevWakeupX = cx;
            prevSleepX = cx;

            arcRect = new RectF(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius
            );
            isInit = true;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawBitmap(bitmap, null, arcRect, null);
        canvas.drawCircle(cx, cy, radius, backgroundPaint);
        canvas.save();
        canvas.rotate(-90, cx, cy);


        if (sleepRadian > wakeupRadian) {

            canvas.drawArc(
                    arcRect,
                    (float) Math.toDegrees(sleepRadian),
                    (float) (Math.toDegrees(2 * PI) - (float) Math.toDegrees(sleepRadian) + (float) Math.toDegrees(wakeupRadian)),
                    false,
                    arcPaint
            );
        } else {

            canvas.drawArc(
                    arcRect,
                    (float) Math.toDegrees(sleepRadian),
                    (float) (Math.toDegrees(wakeupRadian) - Math.toDegrees(sleepRadian)),
                    false,
                    arcPaint
            );
        }
        canvas.restore();
        canvas.save();

        canvas.rotate((float) Math.toDegrees(sleepRadian), cx, cy);
        canvas.drawCircle(cx, cy - radius, strokeWidth / 2, sleepPaint);
        canvas.restore();
        canvas.save();

        canvas.rotate((float) Math.toDegrees(wakeupRadian), cx, cy);
        canvas.drawCircle(cx, cy - radius, strokeWidth / 2, wakePaint);
        canvas.restore();
        canvas.save();

        drawPeriod(canvas);

    }

    private void drawPeriod(Canvas canvas) {
        int h = (int) period;
        int m = (int) ((period - h) * 60);
        String s = String.format("%02d:%02d", h, m);
        canvas.drawText(s, cx - getWidthOfText() / 2, cy + getHeightOfText() / 2, periodPaint);
    }

    private float getWidthOfText() {
        Rect rect = new Rect();
        periodPaint.getTextBounds("03:00", 0, 5, rect);
        return rect.width();
    }
    private float getHeightOfText() {
        Rect rect = new Rect();
        periodPaint.getTextBounds("03:00", 0, 5, rect);
        return rect.height();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isWakeupButton(event.getX(), event.getY())) {
                    isWakeupButton = true;
                } else if (isSleepButton(event.getX(), event.getY())) {
                    isSleepButton = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSleepButton) {
                    sleepRadian = getRadian(event.getX(), event.getY());
                    handleSleepTime(event.getX(), event.getY());
                    invalidate();

                } else if (isWakeupButton) {
                    wakeupRadian = getRadian(event.getX(), event.getY());
                    handleWakeTime(event.getX(), event.getY());
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (isSleepButton) {
                    isSleepButton = false;
                } else if (isWakeupButton) {
                    isWakeupButton = false;
                }
                break;
        }
        return true;
    }

    private void handleSleepTime(float x, float y) {
        if (x < cx && x < prevSleepX && prevSleepX >= cx && y < cy) {
            float radianFromPrevXToRoot = prevSleepRadian;
            float radianFromRootToCurrent = (float) (Math.toRadians(360) - sleepRadian);
            cOfSleep -= ((PI * radius * Math.toDegrees(radianFromPrevXToRoot + radianFromRootToCurrent)) / 180);
        } else if (x > cx && x > prevSleepX && prevSleepX < cx && y < cy) {
            float radianFromPrevXToRoot = (float) (Math.toRadians(360) - prevSleepRadian);
            float radianFromRootToCurrent = sleepRadian;
            cOfSleep += ((PI * radius * Math.toDegrees(radianFromPrevXToRoot + radianFromRootToCurrent)) / 180);
        } else {
            if (sleepRadian > prevSleepRadian) {
                cOfSleep += ((PI * radius * Math.toDegrees(sleepRadian - prevSleepRadian)) / 180);
            } else {
                cOfSleep -= ((PI * radius * Math.toDegrees(prevSleepRadian - sleepRadian)) / 180);
            }
        }

        if (cOfSleep >= (c * 2)) {
            cOfSleep = cOfSleep - (c * 2);
        }

        if (cOfSleep < -(c * 2)) {
            cOfSleep = cOfSleep + (c * 2);
        }

        if (cOfSleep < 0) {
            cOfSleep = (2 * c) + cOfSleep;
        }

        sleepTime = (float) ((12 / (2 * PI)) * sleepRadian);
        if (cOfSleep > c) {
            sleepTime = sleepTime + 12;
        }
        if (onTimeChange != null) {
            onTimeChange.onSleepTimeChange(sleepTime);
        }

        period();

        prevSleepRadian = sleepRadian;
        prevSleepX = x;
    }

    private void handleWakeTime(float x, float y) {

        if (x < cx && x < prevWakeupX && prevWakeupX >= cx && y < cy) { // x từ điểm 1h về 23h
            float radianFromPrevXToRoot = prevWakeupRadian;
            float radianFromRootToCurrent = (float) (Math.toRadians(360) - wakeupRadian);
            cOfWakeup -= ((PI * radius * Math.toDegrees(radianFromPrevXToRoot + radianFromRootToCurrent)) / 180);
        } else if (x > cx && x > prevWakeupX && prevWakeupX < cx && y < cy) { // x từ 23h đến 1h
            float radianFromPrevXToRoot = (float) (Math.toRadians(360) - prevWakeupRadian);
            float radianFromRootToCurrent = wakeupRadian;
            cOfWakeup += ((PI * radius * Math.toDegrees(radianFromPrevXToRoot + radianFromRootToCurrent)) / 180);
        } else {
            if (wakeupRadian > prevWakeupRadian) {
                cOfWakeup += ((PI * radius * Math.toDegrees(wakeupRadian - prevWakeupRadian)) / 180);
            } else {
                cOfWakeup -= ((PI * radius * Math.toDegrees(prevWakeupRadian - wakeupRadian)) / 180);
            }
        }

        if (cOfWakeup >= (c * 2)) {
            cOfWakeup = cOfWakeup - (c * 2);
        }

        if (cOfWakeup < -(c * 2)) {
            cOfWakeup = cOfWakeup + (c * 2);
        }

        if (cOfWakeup < 0) {
            cOfWakeup = (2 * c) + cOfWakeup;
        }

        wakeTime = (float) ((12 / (2 * PI)) * wakeupRadian);
        if (cOfWakeup > c) {
            wakeTime = wakeTime + 12;
        }

        if (onTimeChange != null) {
            onTimeChange.onWakeupTimeChange(wakeTime);
        }

        period();

        prevWakeupRadian = wakeupRadian;
        prevWakeupX = x;
    }

    private boolean isSleepButton(float x, float y) {
        float r = radius;
        float x1 = (float) (cx + r * Math.sin(sleepRadian));
        float y1 = (float) (cy - r * Math.cos(sleepRadian));
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)) < (strokeWidth / 2f);
    }

    private boolean isWakeupButton(float x, float y) {
        float r = radius;
        float x1 = (float) (cx + r * Math.sin(wakeupRadian));
        float y1 = (float) (cy - r * Math.cos(wakeupRadian));
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)) < (strokeWidth / 2f);
    }


    private float getRadian(float x, float y) {
        float alpha = (float) Math.atan((x - cx) / (cy - y));
        // Quadrant
        if (x > cx && y > cy) {
            // 2
            alpha = (float) (alpha + PI);
        } else if (x < cx && y > cy) {
            // 3
            alpha = (float) (alpha + PI);
        } else if (x < cx && y < cy) {
            // 4
            alpha = (float) (alpha + (2 * PI));
        }
        return alpha;
    }

    public void period() {
        period = wakeTime - sleepTime;
        if (period < 0) {
            period = 24 + period;
        }
    }

    public void setListener(OnTimeChange onTimeChange) {
        this.onTimeChange = onTimeChange;
    }

}



