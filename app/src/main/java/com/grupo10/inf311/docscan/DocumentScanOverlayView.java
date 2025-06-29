package com.grupo10.inf311.docscan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DocumentScanOverlayView extends View {
    private Paint borderPaint;
    private Paint transparentPaint;
    private Paint textPaint;
    private RectF documentRect;
    private float cornerRadius = 15f;

    public DocumentScanOverlayView(Context context) {
        super(context);
        init();
    }

    public DocumentScanOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint para a borda
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        // Paint para o fundo semi-transparente
        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setColor(Color.parseColor("#80000000"));

        // Paint para o texto
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK);

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Proporções padrão do RG brasileiro (85.6mm × 53.98mm ≈ 1.58:1)
        float rgRatio = 1.58f;
        float rectWidth, rectHeight;

        if (w > h) {
            // Modo paisagem
            rectWidth = w * 0.8f;
            rectHeight = rectWidth / rgRatio;

            // Ajustar se a altura for maior que a disponível
            if (rectHeight > h * 0.6f) {
                rectHeight = h * 0.6f;
                rectWidth = rectHeight * rgRatio;
            }
        } else {
            // Modo retrato
            rectHeight = h * 0.35f;
            rectWidth = rectHeight * rgRatio;

            // Ajustar se a largura for maior que a disponível
            if (rectWidth > w * 0.9f) {
                rectWidth = w * 0.9f;
                rectHeight = rectWidth / rgRatio;
            }
        }

        // Centralizar o retângulo
        float left = (w - rectWidth) / 2;
        float top = (h - rectHeight) / 2;

        documentRect = new RectF(left, top, left + rectWidth, top + rectHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (documentRect == null) return;

        // Desenhar overlay semi-transparente
        canvas.drawRect(0, 0, getWidth(), getHeight(), transparentPaint);

        // Criar área transparente para o documento
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(documentRect, cornerRadius, cornerRadius, transparentPaint);
        transparentPaint.setXfermode(null);

        // Desenhar borda
        canvas.drawRoundRect(documentRect, cornerRadius, cornerRadius, borderPaint);

        // Desenhar instruções
        float centerX = getWidth() / 2f;

        // Texto principal
        canvas.drawText("Posicione seu RG aqui", centerX, documentRect.top - 80, textPaint);

        // Texto secundário
        textPaint.setTextSize(32f);
        canvas.drawText("Mantenha o documento bem iluminado", centerX, documentRect.top - 40, textPaint);

        // Restaurar tamanho do texto
        textPaint.setTextSize(48f);

        // Desenhar cantos para melhor visualização
        drawCorners(canvas);
    }

    private void drawCorners(Canvas canvas) {
        Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(Color.parseColor("#77D8C2"));
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(6f);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        float cornerLength = 30f;

        // Canto superior esquerdo
        canvas.drawLine(documentRect.left, documentRect.top + cornerLength,
                documentRect.left, documentRect.top, cornerPaint);
        canvas.drawLine(documentRect.left, documentRect.top,
                documentRect.left + cornerLength, documentRect.top, cornerPaint);

        // Canto superior direito
        canvas.drawLine(documentRect.right - cornerLength, documentRect.top,
                documentRect.right, documentRect.top, cornerPaint);
        canvas.drawLine(documentRect.right, documentRect.top,
                documentRect.right, documentRect.top + cornerLength, cornerPaint);

        // Canto inferior esquerdo
        canvas.drawLine(documentRect.left, documentRect.bottom - cornerLength,
                documentRect.left, documentRect.bottom, cornerPaint);
        canvas.drawLine(documentRect.left, documentRect.bottom,
                documentRect.left + cornerLength, documentRect.bottom, cornerPaint);

        // Canto inferior direito
        canvas.drawLine(documentRect.right - cornerLength, documentRect.bottom,
                documentRect.right, documentRect.bottom, cornerPaint);
        canvas.drawLine(documentRect.right, documentRect.bottom,
                documentRect.right, documentRect.bottom - cornerLength, cornerPaint);
    }

    public RectF getDocumentRect() {
        return documentRect;
    }
}