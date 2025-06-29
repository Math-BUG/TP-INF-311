package com.grupo10.inf311.docscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class CropOverlayView extends View {

    private static final float TOUCH_TOLERANCE = 50f;
    private static final float CIRCLE_RADIUS = 20f;

    private Paint linePaint;
    private Paint circlePaint;
    private Paint fillPaint;

    private List<PointF> cropPoints = new ArrayList<>();
    private int activePointIndex = -1;
    private ImageView associatedImageView;
    private Bitmap associatedBitmap;

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(Color.argb(50, 0, 255, 0));
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
    }

    public void setupForBitmap(Bitmap bitmap, ImageView imageView) {
        this.associatedBitmap = bitmap;
        this.associatedImageView = imageView;

        // Calcular as dimensões da imagem na ImageView
        post(() -> {
            if (associatedImageView != null && associatedBitmap != null) {
                RectF imageRect = getImageRectInImageView();
                initializeDefaultCropPoints(imageRect);
                invalidate();
            }
        });
    }

    private RectF getImageRectInImageView() {
        if (associatedImageView == null || associatedBitmap == null) {
            return new RectF(0, 0, getWidth(), getHeight());
        }

        float imageWidth = associatedBitmap.getWidth();
        float imageHeight = associatedBitmap.getHeight();
        float viewWidth = associatedImageView.getWidth();
        float viewHeight = associatedImageView.getHeight();

        float scaleX = viewWidth / imageWidth;
        float scaleY = viewHeight / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;

        float left = (viewWidth - scaledWidth) / 2;
        float top = (viewHeight - scaledHeight) / 2;

        return new RectF(left, top, left + scaledWidth, top + scaledHeight);
    }

    private void initializeDefaultCropPoints(RectF imageRect) {
        cropPoints.clear();

        float margin = 50f;

        // Cantos do documento (no sentido horário)
        cropPoints.add(new PointF(imageRect.left + margin, imageRect.top + margin)); // Superior esquerdo
        cropPoints.add(new PointF(imageRect.right - margin, imageRect.top + margin)); // Superior direito
        cropPoints.add(new PointF(imageRect.right - margin, imageRect.bottom - margin)); // Inferior direito
        cropPoints.add(new PointF(imageRect.left + margin, imageRect.bottom - margin)); // Inferior esquerdo
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cropPoints.size() == 4) {
            // Desenhar área selecionada
            Path path = new Path();
            path.moveTo(cropPoints.get(0).x, cropPoints.get(0).y);
            for (int i = 1; i < cropPoints.size(); i++) {
                path.lineTo(cropPoints.get(i).x, cropPoints.get(i).y);
            }
            path.close();
            canvas.drawPath(path, fillPaint);

            // Desenhar linhas conectando os pontos
            for (int i = 0; i < cropPoints.size(); i++) {
                PointF start = cropPoints.get(i);
                PointF end = cropPoints.get((i + 1) % cropPoints.size());
                canvas.drawLine(start.x, start.y, end.x, end.y, linePaint);
            }

            // Desenhar círculos nos pontos
            for (PointF point : cropPoints) {
                canvas.drawCircle(point.x, point.y, CIRCLE_RADIUS, circlePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activePointIndex = findNearestPoint(x, y);
                return activePointIndex != -1;

            case MotionEvent.ACTION_MOVE:
                if (activePointIndex != -1) {
                    cropPoints.get(activePointIndex).set(x, y);
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                activePointIndex = -1;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private int findNearestPoint(float x, float y) {
        for (int i = 0; i < cropPoints.size(); i++) {
            PointF point = cropPoints.get(i);
            float distance = (float) Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2));
            if (distance <= TOUCH_TOLERANCE) {
                return i;
            }
        }
        return -1;
    }

    public List<PointF> getCropPoints() {
        if (associatedImageView == null || associatedBitmap == null) {
            return new ArrayList<>(cropPoints);
        }

        // Converter pontos da view para coordenadas da imagem
        List<PointF> imagePoints = new ArrayList<>();
        RectF imageRect = getImageRectInImageView();

        float scaleX = associatedBitmap.getWidth() / imageRect.width();
        float scaleY = associatedBitmap.getHeight() / imageRect.height();

        for (PointF viewPoint : cropPoints) {
            float imageX = (viewPoint.x - imageRect.left) * scaleX;
            float imageY = (viewPoint.y - imageRect.top) * scaleY;
            imagePoints.add(new PointF(imageX, imageY));
        }

        return imagePoints;
    }
}