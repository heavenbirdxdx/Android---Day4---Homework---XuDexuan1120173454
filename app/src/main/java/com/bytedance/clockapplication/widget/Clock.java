package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();
    private Runnable update;
    final Handler handler = new Handler();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;



    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;

        update = new Runnable() {
            @Override
            public void run() {
                invalidate();
                handler.postDelayed(update, 500);
            }
        };
        handler.post(update);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }
//        postInvalidateDelayed(1000);
    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */

    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        TextPaint paintHoursValues =  new TextPaint();
        paintHoursValues.setStyle(Paint.Style.FILL_AND_STROKE);
        paintHoursValues.setStrokeCap(Paint.Cap.ROUND);
        paintHoursValues.setTextSize(mWidth * 0.06f);
        paintHoursValues.setColor(hoursValuesColor);
        paintHoursValues.setTextAlign(Paint.Align.CENTER);
        paintHoursValues.setAntiAlias(true);

        int rPadded = mCenterX - (int) (mWidth * 0.1f);
        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {
            if (!((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)){
                int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
                int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)) + 20);
                String HourValues = String.valueOf((15-i/30)%12);
                if((15-i/30)%12 == 0)
                {
                    HourValues = "12";
                }
                else if((15-i/30)%12 < 10)
                {
                    HourValues = "0" + HourValues;
                }
                canvas.drawText(HourValues, startX, startY, paintHoursValues);
            }
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        Paint paintSecondsNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSecondsNeedle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintSecondsNeedle.setStrokeCap(Paint.Cap.ROUND);
        paintSecondsNeedle.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintSecondsNeedle.setColor(secondsNeedleColor);

        Paint paintMinutesNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMinutesNeedle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintMinutesNeedle.setStrokeCap(Paint.Cap.ROUND);
        paintMinutesNeedle.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH * 1.5f);
        paintMinutesNeedle.setColor(minutesNeedleColor);

        Paint paintHoursNeedle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHoursNeedle.setStyle(Paint.Style.FILL_AND_STROKE);
        paintHoursNeedle.setStrokeCap(Paint.Cap.ROUND);
        paintHoursNeedle.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH * 2.0f);
        paintHoursNeedle.setColor(hoursValuesColor);

        int rPaddedSecondsNeedle = mCenterX - (int) (mWidth * 0.15f);
        int rPaddedMinutesNeedle = mCenterX - (int) (mWidth * 0.23f);
        int rPaddedHoursNeedle = mCenterX - (int) (mWidth * 0.30f);
        int startXNeedle = (int) (mCenterX);
        int startYNeedle = (int) (mCenterX);

        int stopXSecondsNeedle = (int) (mCenterX + rPaddedSecondsNeedle * Math.cos(Math.toRadians(((15 - second)%60 * 6))));
        int stopYSecondsNeedle = (int) (mCenterX - rPaddedSecondsNeedle * Math.sin(Math.toRadians(((15 - second)%60 * 6))));

        int stopXMinutesNeedle = (int) (mCenterX + rPaddedMinutesNeedle * Math.cos(Math.toRadians(((15 - (minute+(float)(second)/60.0))%60 * 6))));
        int stopYMinutesNeedle = (int) (mCenterX - rPaddedMinutesNeedle * Math.sin(Math.toRadians(((15 - (minute+(float)(second)/60.0))%60 * 6))));

        int stopXHoursNeedle = (int) (mCenterX + rPaddedHoursNeedle * Math.cos(Math.toRadians(((15 - (hour+(float)(minute)/60.0+(float)(second)/3600.0))%12 * 30))));
        int stopYHoursNeedle = (int) (mCenterX - rPaddedHoursNeedle * Math.sin(Math.toRadians(((15 - (hour+(float)(minute)/60.0+(float)(second)/3600.0))%12 * 30))));

        canvas.drawLine(startXNeedle, startYNeedle, stopXSecondsNeedle, stopYSecondsNeedle, paintSecondsNeedle);
        canvas.drawLine(startXNeedle, startYNeedle, stopXMinutesNeedle, stopYMinutesNeedle, paintMinutesNeedle);
        canvas.drawLine(startXNeedle, startYNeedle, stopXHoursNeedle, stopYHoursNeedle, paintHoursNeedle);

    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paintCenterInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterInner.setColor(centerInnerColor);
        paintCenterInner.setStyle(Paint.Style.FILL);

        Paint paintCenterOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenterOuter.setColor(centerOuterColor);
        paintCenterOuter.setStyle(Paint.Style.FILL);

        canvas.drawCircle(mCenterX, mCenterY, 30, paintCenterOuter);
        canvas.drawCircle(mCenterX, mCenterY, 20, paintCenterInner);

    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

    public void removeHandler(){
        handler.removeCallbacks(update);//移除Runnable对象
    };
}