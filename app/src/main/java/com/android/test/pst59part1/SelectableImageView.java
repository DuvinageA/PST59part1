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
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SelectableImageView extends android.support.v7.widget.AppCompatImageView implements View.OnLongClickListener {

    private State state;
    private Mode mode;
    private Matrix matrix;
    private Matrix savedMatrix;
    private PointF start;
    private PointF mid;
    private float oldDist;
    private RectF imageRect;
    private RectF originalRect;
    private boolean isMapped;
    private float minZoom;
    private float currentZoom;
    private PointF currentTranslation;
    private PointF borderTopLeft;
    private PointF borderBottomRight;
    private PointF currentTopLeft;
    private PointF currentBottomRight;
    private List<RectF> adaptedSelections;
    private PointF drawStart;
    private int currentSelection;
    private float[] drawValues;
    private Matrix drawMatrix;
    private RectF drawRect;
    private Paint paint;
    private float circleRadius;
    private int selectedCircle;
    private boolean inSelectionZone;
    private boolean isZoomingSelection;
    private float oldDistSelection;
    private PointF midSelection;
    private TreatedImage treatedImage;

    enum Mode {
        DISPLAYING,
        EDITING
    }

    enum State {
        WAITING,
        DRAGGING,
        ZOOMING,
        DRAWING,
    }

    public SelectableImageView(Context context) {
        super(context);
        state = State.WAITING;
        mode = Mode.DISPLAYING;
        adaptedSelections = new ArrayList<>();
        matrix = new Matrix();
        savedMatrix = new Matrix();
        start = new PointF();
        mid = new PointF();
        imageRect = new RectF();
        originalRect = new RectF();
        currentTranslation = new PointF();
        borderTopLeft = new PointF();
        borderBottomRight = new PointF();
        currentTopLeft = new PointF();
        currentBottomRight = new PointF();
        drawStart = new PointF();
        drawValues = new float[9];
        drawMatrix = new Matrix();
        drawRect = new RectF();
        paint = new Paint();
        midSelection = new PointF();
        selectedCircle = -1;
        oldDist = 1f;
        circleRadius = 30f;
        setOnLongClickListener(this);
    }

    public SelectableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        state = State.WAITING;
        mode = Mode.DISPLAYING;
        adaptedSelections = new ArrayList<>();
        matrix = new Matrix();
        savedMatrix = new Matrix();
        start = new PointF();
        mid = new PointF();
        imageRect = new RectF();
        originalRect = new RectF();
        currentTranslation = new PointF();
        borderTopLeft = new PointF();
        borderBottomRight = new PointF();
        currentTopLeft = new PointF();
        currentBottomRight = new PointF();
        drawStart = new PointF();
        drawValues = new float[9];
        drawMatrix = new Matrix();
        drawRect = new RectF();
        paint = new Paint();
        midSelection = new PointF();
        selectedCircle = -1;
        oldDist = 1f;
        circleRadius = 30f;
        setOnLongClickListener(this);
    }

    public interface OnEditingModeListener {
        void notifyEditingModeChange(Mode mode);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mode == Mode.DISPLAYING && state == State.WAITING && imageRect.contains(drawStart.x, drawStart.y)) {
            state = State.DRAWING;
            currentSelection = treatedImage.getDescription().keySet().size();
            RectF adaptedRect = new RectF(drawStart.x, drawStart.y, drawStart.x, drawStart.y);
            adaptedSelections.add(adaptedRect);
            RectF originalRect = new RectF(adaptedRect);
            treatedImage.addSelection(originalRect);
            invalidate();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        RectF tempRect = new RectF(imageRect);
        imageRect = new RectF(originalRect);
        List<RectF> tempSelections = new ArrayList<>();
        for (int i = 0; i < treatedImage.getDescription().keySet().size(); i++) {
            tempSelections.add(new RectF(adaptedSelections.get(0)));
            adaptedSelections.add(new RectF(treatedImage.getSelection(i)));
            adaptedSelections.remove(0);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                if (state == State.WAITING && mode == Mode.DISPLAYING) {
                    currentSelection = getNearestContainingSelection(event, tempSelections);
                    drawStart.set(event.getX(), event.getY());
                } else if (mode == Mode.EDITING) {
                    editingModeInitializer(event, tempSelections);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (state == State.WAITING && event.getPointerCount() == 2 && selectedCircle == -1 && !inSelectionZone) {
                    zoomingModeTrigger(event);
                } else if (state == State.WAITING && event.getPointerCount() == 2 && selectedCircle == -1 && inSelectionZone) {
                    oldDistSelection = spacing(event);
                    if (oldDistSelection > 10f) {
                        midPoint(midSelection, event);
                        isZoomingSelection = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (state == State.DRAGGING) {
                    state = State.WAITING;
                } else if (state == State.DRAWING) {
                    mode = Mode.EDITING;
                    ((OnEditingModeListener)getContext()).notifyEditingModeChange(mode);
                    state = State.WAITING;
                } else if (mode == Mode.EDITING) {
                    selectedCircle = -1;
                    inSelectionZone = false;
                } else if (state == State.WAITING) {
                    if (currentSelection != -1) {
                        mode = Mode.EDITING;
                        ((OnEditingModeListener)getContext()).notifyEditingModeChange(mode);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (state == State.ZOOMING) {
                    state = State.WAITING;
                } else if (state == State.WAITING) {
                    isZoomingSelection = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (state == State.DRAGGING) {
                    draggingModeTreatment(event, tempRect);
                } else if (state == State.ZOOMING) {
                    zoomingModeTreatment(event);
                } else if (state == State.WAITING) {
                    if (tempRect.contains(event.getX(), event.getY()) && distance(drawStart, event.getX(), event.getY()) > 10f && selectedCircle == -1 && !inSelectionZone) {
                        draggingModeTrigger(event, tempRect);
                    } else if (selectedCircle != -1) {
                        circleSelectionResize(event, tempRect);
                    } else if (inSelectionZone && !isZoomingSelection) {
                        moveSelection(event, tempRect, tempSelections);
                    } else if (inSelectionZone) {
                        selectionZoom(event, tempRect, tempSelections);
                    }
                } else if (state == State.DRAWING && mode == Mode.DISPLAYING) {
                    drawingModeTreatment(event);
                }
                break;
        }
        setImageMatrix(matrix);
        matrix.mapRect(imageRect);
        for (RectF selection : adaptedSelections) {
            matrix.mapRect(selection);
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isMapped) {
            computeImageRect();
            computeAndApplyMinZoom();
            isMapped = true;
        }
        if (mode == Mode.DISPLAYING && state != State.DRAWING) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            for (RectF selection : adaptedSelections) {
                canvas.drawRect(selection, paint);
            }
        }
        if (state == State.DRAWING && mode == Mode.DISPLAYING) {
            drawingSelection(canvas);
        } else if (mode == Mode.EDITING) {
            editingSelection(canvas);
        }
    }

    private int getNearestContainingSelection(final MotionEvent event, List<RectF> tempSelections) {
        final float x = event.getX();
        final float y = event.getY();
        Set<RectF> containingSelections = new TreeSet<>(new Comparator<RectF>() {
            @Override
            public int compare(RectF o1, RectF o2) {
                float value1 = Math.min(x - o1.left, y - o1.top);
                value1 = Math.min(o1.right - x, value1);
                value1 = Math.min(o1.bottom - y, value1);
                float value2 = Math.min(x - o2.left, y - o2.top);
                value2 = Math.min(o2.right - x, value2);
                value2 = Math.min(o2.bottom - y, value2);
                return Float.compare(value1, value2);
            }
        });
        for (RectF selection : tempSelections) {
            if (selection.contains(event.getX(), event.getY()))
                containingSelections.add(selection);
        }
        if (!containingSelections.isEmpty()) {
            return tempSelections.indexOf(((TreeSet<RectF>) containingSelections).first());
        }
        return -1;
    }

    private void drawingModeTreatment(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (x < imageRect.left) {
            x = imageRect.left;
        } else if (x > imageRect.right) {
            x = imageRect.right;
        } else if (y < imageRect.top) {
            y = imageRect.top;
        } else if (y > imageRect.bottom) {
            y = imageRect.bottom;
        }
        adaptedSelections.get(currentSelection).set(drawStart.x, drawStart.y, x, y);
        RectF originalRect = treatedImage.getSelection(currentSelection);
        String value = treatedImage.removeSelectionFromMap(currentSelection);
        originalRect.set(drawStart.x, drawStart.y, x, y);
        Matrix mat = new Matrix();
        float[] values = new float[9];
        matrix.getValues(values);
        mat.postTranslate(-1 * values[Matrix.MTRANS_X], -1 * values[Matrix.MTRANS_Y]);
        mat.postScale(1 / values[Matrix.MSCALE_X], 1 / values[Matrix.MSCALE_Y]);
        mat.mapRect(originalRect);
        treatedImage.resetSelectionInMap(currentSelection, value);
    }

    private void selectionZoom(MotionEvent event, RectF tempRect, List<RectF> tempSelections) {
        RectF rect = tempSelections.get(currentSelection);
        RectF originalRect = treatedImage.getSelection(currentSelection);
        String value = treatedImage.removeSelectionFromMap(currentSelection);
        originalRect.set(rect);
        float newDist = spacing(event);
        if (newDist > 10f) {
            Matrix mat = new Matrix();
            float scale = newDist / oldDistSelection;
            mat.setScale(scale, scale, midSelection.x, midSelection.y);
            mat.mapRect(originalRect);
            oldDistSelection = spacing(event);
            midPoint(midSelection, event);
        }
        originalRect.left = originalRect.left > tempRect.left ? originalRect.left:tempRect.left;
        originalRect.top = originalRect.top > tempRect.top ? originalRect.top:tempRect.top;
        originalRect.right = originalRect.right < tempRect.right ? originalRect.right:tempRect.right;
        originalRect.bottom = originalRect.bottom < tempRect.bottom ? originalRect.bottom:tempRect.bottom;
        float[] values = new float[9];
        matrix.getValues(values);
        Matrix mat = new Matrix();
        mat.postTranslate(-1 * values[Matrix.MTRANS_X], -1 * values[Matrix.MTRANS_Y]);
        mat.postScale(1 / values[Matrix.MSCALE_X], 1 / values[Matrix.MSCALE_Y]);
        mat.mapRect(originalRect);
        treatedImage.resetSelectionInMap(currentSelection, value);
        adaptedSelections.get(currentSelection).set(originalRect);
    }

    private void moveSelection(MotionEvent event, RectF tempRect, List<RectF> tempSelections) {
        float x = event.getX();
        float y = event.getY();
        RectF rect = tempSelections.get(currentSelection);
        if (rect.left + x - drawStart.x < tempRect.left) {
            x = tempRect.left - rect.left + drawStart.x;
        } else if (rect.right + x - drawStart.x > tempRect.right) {
            x = tempRect.right - rect.right + drawStart.x;
        }
        if (rect.top + y - drawStart.y < tempRect.top) {
            y = tempRect.top - rect.top + drawStart.y;
        } else if (rect.bottom + y - drawStart.y > tempRect.bottom) {
            y = tempRect.bottom - rect.bottom + drawStart.y;
        }
        RectF originalRect = treatedImage.getSelection(currentSelection);
        String value = treatedImage.removeSelectionFromMap(currentSelection);
        originalRect.set(rect.left + x - drawStart.x, rect.top + y - drawStart.y, rect.right + x - drawStart.x, rect.bottom + y - drawStart.y);
        Matrix mat = new Matrix();
        float[] values = new float[9];
        matrix.getValues(values);
        mat.postTranslate(-1 * values[Matrix.MTRANS_X], -1 * values[Matrix.MTRANS_Y]);
        mat.postScale(1 / values[Matrix.MSCALE_X], 1 / values[Matrix.MSCALE_Y]);
        mat.mapRect(originalRect);
        treatedImage.resetSelectionInMap(currentSelection, value);
        adaptedSelections.get(currentSelection).set(originalRect);
        drawStart.set(x, y);
    }

    private void circleSelectionResize(MotionEvent event, RectF tempRect) {
        float x = event.getX();
        float y = event.getY();
        if (x < tempRect.left) {
            x = tempRect.left;
        } else if (x > tempRect.right) {
            x = tempRect.right;
        } else if (y < tempRect.top) {
            y = tempRect.top;
        } else if (y > tempRect.bottom) {
            y = tempRect.bottom;
        }
        if (x < drawStart.x && y < drawStart.y) {
            selectedCircle = 0;
        } else if (x < drawStart.x && y > drawStart.y) {
            selectedCircle = 2;
        } else if (x > drawStart.x && y < drawStart.y) {
            selectedCircle = 1;
        } else if (x > drawStart.x && y > drawStart.y) {
            selectedCircle = 3;
        }
        RectF originalRect = treatedImage.getSelection(currentSelection);
        String value = treatedImage.removeSelectionFromMap(currentSelection);
        originalRect.set(drawStart.x, drawStart.y, x, y);
        Matrix mat = new Matrix();
        float[] values = new float[9];
        matrix.getValues(values);
        mat.postTranslate(-1 * values[Matrix.MTRANS_X], -1 * values[Matrix.MTRANS_Y]);
        mat.postScale(1 / values[Matrix.MSCALE_X], 1 / values[Matrix.MSCALE_Y]);
        mat.mapRect(originalRect);
        treatedImage.resetSelectionInMap(currentSelection, value);
        adaptedSelections.get(currentSelection).set(originalRect);
    }

    private void editingModeInitializer(MotionEvent event, List<RectF> tempSelections) {
        RectF rect = tempSelections.get(currentSelection);
        float x = event.getX();
        float y = event.getY();
        if (Math.pow((rect.left < rect.right ? rect.left : rect.right) - x, 2) + Math.pow((rect.top < rect.bottom ? rect.top : rect.bottom) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
            selectedCircle = 0;
            drawStart.set(rect.right, rect.bottom);
        } else if (Math.pow((rect.left > rect.right ? rect.left : rect.right) - x, 2) + Math.pow((rect.top < rect.bottom ? rect.top : rect.bottom) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
            selectedCircle = 1;
            drawStart.set(rect.left, rect.bottom);
        } else if (Math.pow((rect.left < rect.right ? rect.left : rect.right) - x, 2) + Math.pow((rect.top > rect.bottom ? rect.top : rect.bottom) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
            selectedCircle = 2;
            drawStart.set(rect.right, rect.top);
        } else if (Math.pow((rect.left > rect.right ? rect.left : rect.right) - x, 2) + Math.pow((rect.top > rect.bottom ? rect.top : rect.bottom) - y, 2) <= Math.pow(1.1d * circleRadius, 2)) {
            selectedCircle = 3;
            drawStart.set(rect.left, rect.top);
        } else if (x > (rect.left < rect.right ? rect.left : rect.right) && x < (rect.left > rect.right ? rect.left : rect.right) && y > (rect.top < rect.bottom ? rect.top : rect.bottom) && y < (rect.top > rect.bottom ? rect.top : rect.bottom)) {
            inSelectionZone = true;
            drawStart.set(x, y);
        }
    }

    private void editingSelection(Canvas canvas) {
        drawRect.set(adaptedSelections.get(currentSelection));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xD0101010);
        canvas.drawRect(imageRect.left, imageRect.top, drawRect.left, imageRect.bottom, paint);
        canvas.drawRect(drawRect.right, imageRect.top, imageRect.right, imageRect.bottom, paint);
        canvas.drawRect(drawRect.left, imageRect.top, drawRect.right, drawRect.top, paint);
        canvas.drawRect(drawRect.left, drawRect.bottom, drawRect.right, imageRect.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF101010);
        paint.setStrokeWidth(7);
        canvas.drawRect(drawRect, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFFD0D0D0);
        paint.setStrokeWidth(7);
        canvas.drawCircle(drawRect.left, drawRect.top, circleRadius, paint);
        canvas.drawCircle(drawRect.right, drawRect.top, circleRadius, paint);
        canvas.drawCircle(drawRect.left, drawRect.bottom, circleRadius, paint);
        canvas.drawCircle(drawRect.right, drawRect.bottom, circleRadius, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selectedCircle != 0 ? 0xFF101010 : 0xFFA0A0A0);
        canvas.drawCircle(drawRect.left, drawRect.top, circleRadius, paint);
        paint.setColor(selectedCircle != 1 ? 0xFF101010 : 0xFFA0A0A0);
        canvas.drawCircle(drawRect.right, drawRect.top, circleRadius, paint);
        paint.setColor(selectedCircle != 2 ? 0xFF101010 : 0xFFA0A0A0);
        canvas.drawCircle(drawRect.left, drawRect.bottom, circleRadius, paint);
        paint.setColor(selectedCircle != 3 ? 0xFF101010 : 0xFFA0A0A0);
        canvas.drawCircle(drawRect.right, drawRect.bottom, circleRadius, paint);
    }

    private void drawingSelection(Canvas canvas) {
        drawRect.set(adaptedSelections.get(currentSelection));
        matrix.getValues(drawValues);
        drawMatrix.setTranslate(-1 * drawValues[Matrix.MTRANS_X], -1 * drawValues[Matrix.MTRANS_Y]);
        drawMatrix.postScale(1 / drawValues[Matrix.MSCALE_X], 1 / drawValues[Matrix.MSCALE_Y]);
        drawMatrix.mapRect(drawRect);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xAA101010);
        canvas.drawRect(imageRect.left, imageRect.top, drawRect.left, imageRect.bottom, paint);
        canvas.drawRect(drawRect.right, imageRect.top, imageRect.right, imageRect.bottom, paint);
        canvas.drawRect(drawRect.left, imageRect.top, drawRect.right, drawRect.top, paint);
        canvas.drawRect(drawRect.left, drawRect.bottom, drawRect.right, imageRect.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF101010);
        paint.setStrokeWidth(5);
        canvas.drawRect(drawRect, paint);
    }

    private void zoomingModeTreatment(MotionEvent event) {
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

    private void draggingModeTreatment(MotionEvent event, RectF tempRect) {
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
    }

    private void zoomingModeTrigger(MotionEvent event) {
        oldDist = spacing(event);
        if (oldDist > 10f) {
            savedMatrix.set(matrix);
            midPoint(mid, event);
            float[] values = new float[9];
            matrix.getValues(values);
            currentZoom = values[Matrix.MSCALE_X];
            state = State.ZOOMING;
        }
    }

    private void draggingModeTrigger(MotionEvent event, RectF tempRect) {
        savedMatrix.set(matrix);
        start.set(event.getX(), event.getY());
        currentTopLeft.set(tempRect.left, tempRect.top);
        currentBottomRight.set(tempRect.right, tempRect.bottom);
        state = State.DRAGGING;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void computeImageRect() {
        int imageHeight = getDrawable().getIntrinsicHeight();
        int imageWidth = getDrawable().getIntrinsicWidth();
        originalRect.set(0, 0, imageWidth, imageHeight);
        imageRect = new RectF(originalRect);
    }

    private void computeAndApplyMinZoom() {
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
        currentTranslation.set(x, y);
        matrix.postScale(minZoom, minZoom, viewWidth / 2, viewHeight / 2);
        currentZoom = minZoom;
        setImageMatrix(matrix);
        matrix.mapRect(imageRect);
        currentTopLeft.set(imageRect.left, imageRect.top);
        currentTopLeft.set(imageRect.right, imageRect.bottom);
    }

    private float distance(PointF origin, float x, float y) {
        return (float) Math.sqrt(Math.pow(origin.x - x, 2) + Math.pow(origin.y - y, 2));
    }

    public void quitEditingMode() {
        mode = Mode.DISPLAYING;
        ((OnEditingModeListener)getContext()).notifyEditingModeChange(mode);
        invalidate();
    }

    public void setTreatedImage(TreatedImage treatedImage) {
        this.treatedImage = treatedImage;
        if (treatedImage.getDescription().keySet().size() != 0) {
            for (RectF selection : treatedImage.getDescription().keySet()) {
                RectF adaptedSelection = new RectF(selection);
                matrix.mapRect(adaptedSelection);
                adaptedSelections.add(adaptedSelection);
            }
        }
        invalidate();
    }

    public int getCurrentSelection() {
        return currentSelection;
    }

    public void resetCurrentSelection() {
        currentSelection = -1;
    }

    public void removeSelection(int position) {
        treatedImage.removeSelection(position);
        adaptedSelections.remove(position);
    }
}
