package com.android.test.pst59part1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TreatableImageView extends android.support.v7.widget.AppCompatImageView implements View.OnLongClickListener {

    Canvas canvas;
    Paint paint;
    State state;
    Point[] points = new Point[2];
    int circleRadius = 20;
    int selectedCircle = -1;
    boolean isMoving = false;
    int xStart = 0;
    int yStart = 0;

    enum State {
        Waiting,
        Drawing,
        DrawingConfirmed,
        Modifying
    }

    public TreatableImageView(Context context) {
        super(context);
        canvas = new Canvas();
        paint = new Paint();
        state = State.Waiting;
        setOnLongClickListener(this);
        points[0] = new Point();
        points[1] = new Point();
    }

    public TreatableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        canvas = new Canvas();
        paint = new Paint();
        state = State.Waiting;
        setOnLongClickListener(this);
        points[0] = new Point();
        points[1] = new Point();
    }

    @Override
    public boolean onLongClick(View v) {
        if (state == State.Waiting) {
            state = State.Drawing;
            points[1].x = points[0].x;
            points[1].y = points[0].y;
            invalidate();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (state == State.Waiting) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
                        points[0].x = x;
                        points[0].y = y;
                    }
                    break;
            }
        } else if (state == State.Drawing || state == State.DrawingConfirmed) {
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
                        state = State.DrawingConfirmed;
                        points[1].x = x;
                        points[1].y = y;
                        invalidate();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (state == State.DrawingConfirmed) {
                        state = state.Modifying;
                        invalidate();
                    }
                    break;
            }
        } else if (state == State.Modifying) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
                        if (Math.pow((points[0].x < points[1].x ? points[0].x : points[1].x) - x, 2) + Math.pow((points[0].y < points[1].y ? points[0].y : points[1].y) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
                            // If we click on the top left circle
                            selectedCircle = 0;
                        } else if (Math.pow((points[0].x < points[1].x ? points[0].x : points[1].x) - x, 2) + Math.pow((points[0].y > points[1].y ? points[0].y : points[1].y) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
                            // If we click on the bottom left circle
                            selectedCircle = 1;
                        } else if (Math.pow((points[0].x > points[1].x ? points[0].x : points[1].x) - x, 2) + Math.pow((points[0].y < points[1].y ? points[0].y : points[1].y) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
                            // If we click on the top right circle
                            selectedCircle = 2;
                        } else if (Math.pow((points[0].x > points[1].x ? points[0].x : points[1].x) - x, 2) + Math.pow((points[0].y > points[1].y ? points[0].y : points[1].y) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
                            // If we click on the bottom right circle
                            selectedCircle = 3;
                        } else if (x > (points[0].x < points[1].x ? points[0].x : points[1].x) && x < (points[0].x > points[1].x ? points[0].x : points[1].x) && y > (points[0].y < points[1].y ? points[0].y : points[1].y) && y < (points[0].y > points[1].y ? points[0].y : points[1].y)) {
                            isMoving = true;
                            xStart = x;
                            yStart = y;
                        }
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
                        if (selectedCircle == 0) {
                            if (points[0].x < points[1].x) {
                                points[0].x = x;
                            } else {
                                points[1].x = x;
                            }
                            if (points[0].y < points[1].y) {
                                points[0].y = y;
                            } else {
                                points[1].y = y;
                            }
                        } else if (selectedCircle == 1) {
                            if (points[0].x < points[1].x) {
                                points[0].x = x;
                            } else {
                                points[1].x = x;
                            }
                            if (points[0].y > points[1].y) {
                                points[0].y = y;
                            } else {
                                points[1].y = y;
                            }
                        } else if (selectedCircle == 2) {
                            if (points[0].x > points[1].x) {
                                points[0].x = x;
                            } else {
                                points[1].x = x;
                            }
                            if (points[0].y < points[1].y) {
                                points[0].y = y;
                            } else {
                                points[1].y = y;
                            }
                        } else if (selectedCircle == 3) {
                            if (points[0].x > points[1].x) {
                                points[0].x = x;
                            } else {
                                points[1].x = x;
                            }
                            if (points[0].y > points[1].y) {
                                points[0].y = y;
                            } else {
                                points[1].y = y;
                            }
                        } else if (isMoving) {
                            points[0].x += x - xStart;
                            points[1].x += x - xStart;
                            xStart = x;
                            points[0].y += y - yStart;
                            points[1].y += y - yStart;
                            yStart = y;
                        }
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    selectedCircle = -1;
                    invalidate();
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == State.Waiting) {
            canvas.drawColor(Color.TRANSPARENT);
        } else if (state == State.Drawing || state == State.DrawingConfirmed) {
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xD0101010);
            // Draw the left rect
            canvas.drawRect(0, 0, points[0].x < points[1].x ? points[0].x : points[1].x, getHeight(), paint);
            // Draw the top rect
            canvas.drawRect(points[0].x < points[1].x ? points[0].x : points[1].x, 0, points[0].x > points[1].x ? points[0].x : points[1].x, points[0].y < points[1].y ? points[0].y : points[1].y, paint);
            // Draw the right rect
            canvas.drawRect(points[0].x > points[1].x ? points[0].x : points[1].x, 0, getWidth(), getHeight(), paint);
            // Draw the bottom rect
            canvas.drawRect(points[0].x < points[1].x ? points[0].x : points[1].x, points[0].y > points[1].y ? points[0].y : points[1].y, points[0].x > points[1].x ? points[0].x : points[1].x, getBottom(), paint);
        } else if (state == State.Modifying) {
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xD0101010);
            // Draw the left rect
            canvas.drawRect(0, 0, points[0].x < points[1].x ? points[0].x : points[1].x, getHeight(), paint);
            // Draw the top rect
            canvas.drawRect(points[0].x < points[1].x ? points[0].x : points[1].x, 0, points[0].x > points[1].x ? points[0].x : points[1].x, points[0].y < points[1].y ? points[0].y : points[1].y, paint);
            // Draw the right rect
            canvas.drawRect(points[0].x > points[1].x ? points[0].x : points[1].x, 0, getWidth(), getHeight(), paint);
            // Draw the bottom rect
            canvas.drawRect(points[0].x < points[1].x ? points[0].x : points[1].x, points[0].y > points[1].y ? points[0].y : points[1].y, points[0].x > points[1].x ? points[0].x : points[1].x, getBottom(), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xE0000000);
            paint.setStrokeWidth(5);
            canvas.drawRect(points[0].x < points[1].x ? points[0].x : points[1].x,
                    points[0].y < points[1].y ? points[0].y : points[1].y,
                    points[0].x > points[1].x ? points[0].x : points[1].x,
                    points[0].y > points[1].y ? points[0].y : points[1].y, paint);

            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL);
            // Draw the top left circle
            paint.setColor(selectedCircle != 0 ? 0xFF000000 : 0xFFA0A0A0);
            canvas.drawCircle(points[0].x < points[1].x ? points[0].x : points[1].x, points[0].y < points[1].y ? points[0].y : points[1].y, circleRadius, paint);
            // Draw the bottom left circle
            paint.setColor(selectedCircle != 1 ? 0xFF000000 : 0xFFA0A0A0);
            canvas.drawCircle(points[0].x < points[1].x ? points[0].x : points[1].x, points[0].y > points[1].y ? points[0].y : points[1].y, circleRadius, paint);
            // Draw the top right circle
            paint.setColor(selectedCircle != 2 ? 0xFF000000 : 0xFFA0A0A0);
            canvas.drawCircle(points[0].x > points[1].x ? points[0].x : points[1].x, points[0].y < points[1].y ? points[0].y : points[1].y, circleRadius, paint);
            // Draw the bottom right circle
            paint.setColor(selectedCircle != 3 ? 0xFF000000 : 0xFFA0A0A0);
            canvas.drawCircle(points[0].x > points[1].x ? points[0].x : points[1].x, points[0].y > points[1].y ? points[0].y : points[1].y, circleRadius, paint);
        }
    }
}
