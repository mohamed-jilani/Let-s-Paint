package com.jilani.letspaint.CustomView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jilani.letspaint.models.ToolType;

public class PaintView extends View {

    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private int currentColor = 0xFF000000; // Couleur noire par défaut
    private float strokeWidth = 10f;
    private ToolType currentTool = ToolType.BRUSH;
    private int backgroundColor = Color.WHITE;
    private float brushSize = 10f;


    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setColor(currentColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentTool == ToolType.FORK) {
                    // Fourchette = plusieurs lignes en parallèle
                    float offset = 20f;
                    bitmapCanvas.drawLine(x, y - offset, x, y + offset, paint);
                    bitmapCanvas.drawLine(x - offset, y, x + offset, y, paint);
                } else {
                    path.lineTo(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentTool != ToolType.FORK) {
                    bitmapCanvas.drawPath(path, paint);
                    path.reset();
                }
                break;
        }
        invalidate();
        return true;
    }

    // Changer la couleur du pinceau
    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
    }
    public int getColor() {
        return currentColor;
    }


    // Changer l'épaisseur du trait
    public void setStrokeWidth(float width) {
        strokeWidth = width;
        paint.setStrokeWidth(width);
    }

    // Effacer le dessin
    public void clear() {
        bitmap.eraseColor(0xFFFFFFFF); // fond blanc
        invalidate();
    }

    // (Bonus) Sauvegarder le dessin en image
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setToolType(ToolType tool) {
        this.currentTool = tool;

        switch (tool) {
            case BRUSH:
                paint.setStrokeWidth(strokeWidth);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case PENCIL:
                paint.setStrokeWidth(strokeWidth / 2);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.BUTT);
                break;
            case FORK:
                // Fourchette = outil personnalisé
                paint.setStrokeWidth(strokeWidth);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
        }
    }



    public void setBackgroundColor(int color) {
        backgroundColor = color;
        super.setBackgroundColor(color);
        invalidate();
    }

    public int getBackgroundColor1() {
        return backgroundColor;
    }

    public int getBackgroundColor() {
        Drawable background = getBackground();
        if (background instanceof ColorDrawable)
            return ((ColorDrawable) background).getColor();
        else
            return Color.WHITE; // par défaut
    }


    public void setBrushSize(float size) {
        brushSize = size;
        paint.setStrokeWidth(size);
        invalidate();
    }

    public int getBrushSize() {
        return (int) brushSize;
    }

    public Bitmap exportToBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas); // dessine le contenu de la vue sur le bitmap
        return bitmap;
    }


}
