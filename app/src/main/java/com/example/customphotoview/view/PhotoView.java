package com.example.customphotoview.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import com.example.customphotoview.utils.Utils;

import androidx.annotation.Nullable;

public class PhotoView extends View {
    private static final float IMAGE_WIDTH = Utils.dpToPx(300);
    private float OVER_SCALE_FACTOR = 1.5f;
    private Bitmap bitmap;
    private Paint paint;
    private float smallScale;
    private float bigScale;
    private float currentScale;//记录当前比例
    private float originalOffsetX;
    private float originalOffsetY;

    private boolean isEnlarge;//是否可以放大
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;//双指缩放

    private ObjectAnimator scaleAnimator;

    private float offsetX;
    private float offsetY;
    private OverScroller overScroller;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        bitmap = Utils.getBitmap(getResources(), (int) IMAGE_WIDTH);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        gestureDetector = new GestureDetector(context, new PhotoGestureListener());

        scaleGestureDetector = new ScaleGestureDetector(context, new PhotoScaleGestureListener());
        overScroller = new OverScroller(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scaleFaction = (currentScale - smallScale) / (bigScale - smallScale);
        canvas.translate(offsetX * scaleFaction, offsetY * scaleFaction);
        canvas.scale(currentScale, currentScale, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(bitmap, originalOffsetX, originalOffsetY, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //这里转换为float是为了不留白边
        // 求原始偏移量，为了让图片居中
        originalOffsetX = (getWidth() - bitmap.getWidth()) / 2f;
        originalOffsetY = (getHeight() - bitmap.getHeight()) / 2f;
        //宽为全屏时为小缩放
        if ((float) bitmap.getWidth() / bitmap.getHeight()
                > (float) getWidth() / getHeight()) {//宽图，图片横向占满屏幕
            //smallScale 放大后一边全屏，一边留白叫小缩放
            smallScale = (float) getWidth() / bitmap.getWidth();
            // bigScale 放大后一边全屏，一边超出界面叫大缩放
            //bigScale的时候多放大点，这样四个方向就都可以滑动了。
            bigScale = (float) getHeight() / bitmap.getHeight() * OVER_SCALE_FACTOR;

        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * OVER_SCALE_FACTOR;
        }
        currentScale = smallScale;
    }

    //必须重写onTouchEvent，因为GestureDetector里面自己重写了事件处理。
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //响应事件以双指缩放优先
        boolean result = scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            result = gestureDetector.onTouchEvent(event);
        }
        return result;
    }

    class PhotoGestureListener extends GestureDetector.SimpleOnGestureListener {


        //up触发，单击或双击的第一次， up时，如果不是双击的第二次点击，不是长按，则触发
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        //长按 默认300ms触发
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        /**
         * 滚动 -- move
         *
         * @param e1        手指按下
         * @param e2        当前的
         * @param distanceX 旧位置 - 新位置,单位距离
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //只有放大时才能滑动
            if (isEnlarge) {
                offsetX = -distanceX;
                offsetY = -distanceY;
                fixOffset();
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }


        /**
         * up 惯性滑动 -- 大于50dp/s时触发
         *
         * @param velocityX x轴方向运动速度（像素/s）
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (isEnlarge) {
                overScroller.fling((int) offsetX, (int) offsetY, (int) velocityX, (int) velocityY,
                        (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        (int) -(bitmap.getWidth() * bigScale - getWidth()) / 2,
                        (int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        (int) -(bitmap.getHeight() * bigScale - getHeight()) / 2);
                postOnAnimation(new FlingRunner());
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        // 延时100ms触发 -- 处理点击效果
        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        // 只需要关注 onDown 的返回值即可，默认是false，必须为true才表示消费事件
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // 双击的第二次点击down时触发。双击的触发时间 -- 40ms -- 300ms
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isEnlarge = !isEnlarge;
            if (isEnlarge) {
                offsetX = (e.getX() - getWidth() / 2f) - (e.getX() - getWidth() / 2f) * bigScale / smallScale;
                offsetY = (e.getY() - getHeight() / 2f) - (e.getY() - getHeight() / 2f) * bigScale / smallScale;
                fixOffset();
                getScaleAnimator().start();
            } else {
                getScaleAnimator().reverse();
            }
            invalidate();
            return super.onDoubleTap(e);
        }

        // 双击的第二次down、move、up 都触发
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        // 单击按下时触发，双击时不触发，down，up时都可能触发
        // 延时300ms触发TAP事件
        // 300ms以内抬手 -- 才会触发TAP -- onSingleTapConfirmed
        // 300ms 以后抬手 --- 不是双击不是长按，则触发
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

    }

    //属性动画让缩放效果更平滑
    private ObjectAnimator getScaleAnimator() {
        if (scaleAnimator == null) {
            scaleAnimator = ObjectAnimator.ofFloat(this, "currentScale", 0);
        }
        scaleAnimator.setFloatValues(smallScale, bigScale);
        return scaleAnimator;
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
        invalidate();
    }

    private void fixOffset() {
        offsetX = Math.min(offsetX, (bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetX = Math.max(offsetX, -(bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetY = Math.min(offsetY, (bitmap.getHeight() * bigScale - getHeight()) / 2);
        offsetY = Math.max(offsetY, -(bitmap.getHeight() * bigScale - getHeight()) / 2);
    }

    class FlingRunner implements Runnable {

        @Override
        public void run() {
            //判断动画是否还在执行
            if (overScroller.computeScrollOffset()) {
                offsetX = overScroller.getCurrX();
                offsetY = overScroller.getCurrY();
                invalidate();
                postOnAnimation(this);
            }
        }
    }

    class PhotoScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        float initScale;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (currentScale >= smallScale && !isEnlarge) {
                isEnlarge = !isEnlarge;
            }
            currentScale = initScale * detector.getScaleFactor();//缩放因子
            invalidate();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initScale = currentScale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
