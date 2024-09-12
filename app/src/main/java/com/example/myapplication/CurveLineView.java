package com.example.myapplication;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

public class CurveLineView extends View {

    private Paint paint;
    private Path path;

    private int x1, y1, x2, y2, curveRadius, color, lineWidth;

    public CurveLineView(Context context) {
        super(context);
        init();
    }

    public CurveLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurveLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        path = new Path();
    }

    public void setCurvedLineParams(int x1, int y1, int x2, int y2, int curveRadius, int color, int lineWidth) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.curveRadius = curveRadius;
        this.color = color;
        this.lineWidth = lineWidth;
        paint.setStrokeWidth(lineWidth);
        paint.setColor(ContextCompat.getColor(getContext(), color));

        invalidate(); // Redraw the view with new parameters
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Calculate mid point
        int midX = x1 + ((x2 - x1) / 2);
        int midY = y1 + ((y2 - y1) / 2);
        float xDiff = midX - x1;
        float yDiff = midY - y1;
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));

        // Create path
        path.reset();
        path.moveTo(x1, y1);
        path.cubicTo(x1, y1, pointX, pointY, x2, y2);

        // Draw path
        canvas.drawPath(path, paint);
    }
}
