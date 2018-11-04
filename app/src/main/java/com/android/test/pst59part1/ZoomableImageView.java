package com.android.test.pst59part1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ZoomableImageView extends android.support.v7.widget.AppCompatImageView {

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    State state;

    RectF imageRect = new RectF();
    RectF originalRect = new RectF();
    Paint paint = new Paint();
    boolean isMapped = false;
    float minZoom;
    float currentZoom;

    PointF borderTopLeft = new PointF();
    PointF borderBottomRight = new PointF();
    PointF currentTopLeft = new PointF();
    PointF currentBottomRight = new PointF();

    enum State {
        Waiting,
        Dragging,
        Zooming,
        Drawing,
        DrawingConfirmed,
        Modifying
    }

    public ZoomableImageView(Context context) {
        super(context);
        state = State.Waiting;
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        state = State.Waiting;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        RectF tempRect = new RectF(imageRect);
        imageRect = new RectF(originalRect);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                currentTopLeft.set(tempRect.left, tempRect.top);
                currentBottomRight.set(tempRect.right, tempRect.bottom);
                state = State.Dragging;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    currentZoom = tempRect.height() / originalRect.height();
                    state = State.Zooming;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                state = State.Waiting;
                break;
            case MotionEvent.ACTION_MOVE:
                if (state == State.Dragging) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    if (tempRect.width() >= getWidth()) {
                        if (currentTopLeft.x + dx > borderTopLeft.x) {
                            matrix.postTranslate(borderTopLeft.x - currentTopLeft.x, 0);
                        } else if (currentBottomRight.x + dx < borderBottomRight.x) {
                            matrix.postTranslate(borderBottomRight.x - currentBottomRight.x, 0);
                        } else {
                            matrix.postTranslate(dx, 0);
                        }
                    } else {
                        if (currentTopLeft.x + dx < borderTopLeft.x) {
                            matrix.postTranslate(borderTopLeft.x - currentTopLeft.x, 0);
                        } else if (currentBottomRight.x + dx > borderBottomRight.x) {
                            matrix.postTranslate(borderBottomRight.x - currentBottomRight.x, 0);
                        } else {
                            matrix.postTranslate(dx, 0);
                        }
                    }
                    if (tempRect.height() >= getHeight()) {
                        if (currentTopLeft.y + dy > borderTopLeft.y) {
                            matrix.postTranslate(0, borderTopLeft.y - currentTopLeft.y);
                        } else if (currentBottomRight.y + dy < borderBottomRight.y) {
                            matrix.postTranslate(0, borderBottomRight.y - currentBottomRight.y);
                        } else {
                            matrix.postTranslate(0, dy);
                        }
                    } else {
                        if (currentTopLeft.y + dy < borderTopLeft.y) {
                            matrix.postTranslate(0, borderTopLeft.y - currentTopLeft.y);
                        } else if (currentBottomRight.y + dy > borderBottomRight.y) {
                            matrix.postTranslate(0, borderBottomRight.y - currentBottomRight.y);
                        } else {
                            matrix.postTranslate(0, dy);
                        }
                    }
                } else if (state == State.Zooming) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        if (currentZoom * scale >= minZoom) {
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        } else {
                            scale = minZoom / currentZoom;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                }
                break;
        }
        setImageMatrix(matrix);
        matrix.mapRect(imageRect);
        invalidate();
        return true;
    }

    protected float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    protected void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isMapped) {
            computeImageRect();
            computeAndApplyMinZoom();
            isMapped = true;
        }
        /*paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawRect(imageRect, paint);*/
    }

    protected void computeImageRect() {
        int imageHeight = getDrawable().getIntrinsicHeight();
        int imageWidth = getDrawable().getIntrinsicWidth();
        originalRect.set(0, 0, imageWidth, imageHeight);
        imageRect = new RectF(originalRect);
    }

    protected void computeAndApplyMinZoom() {
        int imageHeight = getDrawable().getIntrinsicHeight();
        int imageWidth = getDrawable().getIntrinsicWidth();
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        float x;
        float y;
        if (imageHeight * viewWidth < imageWidth * viewHeight) {
            minZoom = 1f * viewWidth / imageWidth;
            x = (viewWidth - imageWidth) / 2f;
            y = (viewHeight - imageHeight) / 2f;
        } else {
            minZoom = 1f * viewHeight / imageHeight;
            x = (viewWidth - imageWidth) / 2f;
            y = (viewHeight - imageHeight) / 2f;
        }
        borderTopLeft.set(0, 0);
        borderBottomRight.set(viewWidth, viewHeight);
        matrix.postTranslate(x, y);
        matrix.postScale(minZoom, minZoom, viewWidth / 2, viewHeight / 2);
        currentZoom = minZoom;
        setImageMatrix(matrix);
        matrix.mapRect(imageRect);
        currentTopLeft.set(imageRect.left, imageRect.top);
        currentTopLeft.set(imageRect.right, imageRect.bottom);
    }
}
