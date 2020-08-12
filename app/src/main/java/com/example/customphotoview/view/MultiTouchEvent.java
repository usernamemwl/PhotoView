package com.example.customphotoview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.customphotoview.utils.Utils;

import androidx.annotation.Nullable;

public class MultiTouchEvent extends View {

    private static final float IMAGE_WIDTH = Utils.dpToPx(300);
    private Paint paint;
    private Bitmap bitmap;
    private float downX;
    private float downY;
    private float offsetX;
    private float offsetY;
    private float lastOffsetX;
    private float lastOffsetY;
    private int currentPointId;

    public MultiTouchEvent(Context context) {
        this(context, null);
    }

    public MultiTouchEvent(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTouchEvent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bitmap = Utils.getBitmap(getResources(), (int) IMAGE_WIDTH);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();

                lastOffsetX = offsetX;
                lastOffsetY = offsetY;

                currentPointId = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(currentPointId);
                offsetX = lastOffsetX + event.getX(pointerIndex) - downX;
                offsetY = lastOffsetY + event.getY(pointerIndex) - downY;
                invalidate();

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 获取当前按下的index
                int actionIndex = event.getActionIndex();
                currentPointId =event.getPointerId(actionIndex);

               downX = event.getX(actionIndex);
               downY = event.getY(actionIndex);
               lastOffsetX = offsetX;
               lastOffsetY = offsetY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int index = event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerId == currentPointId){
                    if (index==event.getPointerCount()-1){
                        index = event.getPointerCount()-2;
                    }else{
                        index ++;
                    }
                    currentPointId = event.getPointerId(index);

                    downX = event.getX(index);
                    downY = event.getY(index);
                    lastOffsetX = offsetX;
                    lastOffsetY = offsetY;
                }
                break;
        }
        return true;
    }
}
