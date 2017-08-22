package com.minminaya.customviewset.view.rope_bounce_view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by Niwa on 2017/8/20.
 */

public class RopeBounceView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Shader shader;
    private Path mPath = new Path();
    /**
     * 倍数因子
     */
    private int factor = 6;

    private int mScreenHeight = 1920;
    private int mScreenWidth = 1080;

    private float degree = 0;


    private int leftGradientColor;
    private int rightGradientColor;

    /**
     * 左正方形中点
     */
    private PointF leftRectPointF = new PointF(mScreenWidth / 2 - 45 * factor, mScreenHeight / 2);
    /**
     * 右正方形中点
     */
    private PointF rightRectPointF = new PointF(mScreenWidth / 2 + 45 * factor, mScreenHeight / 2);

    private PointF lineLeftEndPointF;
    private PointF lineRightEndPointF;
    private PointF quadControllerPointF;
    private PointF circlePointF;
    /**
     * 正方形的对角线一半
     */
    private int rectDiagonalHalf = 10 * factor;


    /**
     * 球的底部边界点相对水平面高度
     */
    private float circleBottomHeight;


    public void setCircleBottomHeight(float circleBottomHeight) {
        this.circleBottomHeight = circleBottomHeight;
    }


    public void setLeftGradientColor(int leftGradientColor) {
        this.leftGradientColor = leftGradientColor;

    }

    public void setRightGradientColor(int rightGradientColor) {
        this.rightGradientColor = rightGradientColor;
    }

    public void setDegree(float degree) {
        this.degree = degree;
        //左边正方形和线连接点
        lineLeftEndPointF = calculatPoint(leftRectPointF, rectDiagonalHalf, degree);
        //右边角度的是180的（余）数
        lineRightEndPointF = calculatPoint(rightRectPointF, rectDiagonalHalf, 180 - degree);
        quadControllerPointF = calculatQuadControllerPointF(45 * factor, degree);
        circlePointF = new PointF(540f, circleBottomHeight - 10 * factor);
        invalidate();
    }

    public RopeBounceView(Context context) {
        super(context);
        initAnimator();
    }

    public RopeBounceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAnimator();
    }

    public RopeBounceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimator();
    }

    /**
     * 初始化动画引擎
     */
    public void initAnimator() {

        //左边渐变边界点颜色值的变化引擎
        ObjectAnimator leftGradientColorAnimator = ObjectAnimator.ofArgb(this, "leftGradientColor", 0xFFE42A34, 0xFFE72F2C, 0xFFEF2539);
        leftGradientColorAnimator.setDuration(2500);
        leftGradientColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        leftGradientColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        leftGradientColorAnimator.setInterpolator(new LinearInterpolator());
        leftGradientColorAnimator.start();
        //右边渐变边界点颜色值的变化引擎
        ObjectAnimator rightGradientColorAnimator = ObjectAnimator.ofArgb(this, "rightGradientColor", 0xFFD767DF, 0xFFC118D9, 0xFFBD19D3);
        rightGradientColorAnimator.setDuration(2500);
        rightGradientColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rightGradientColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rightGradientColorAnimator.setInterpolator(new LinearInterpolator());
        rightGradientColorAnimator.start();


        //最低往返最高
        final ObjectAnimator circleHeightAnimator2 = ObjectAnimator.ofFloat(this, "circleBottomHeight", 960 + 20 * factor, 960 - 70 * factor);
        circleHeightAnimator2.setDuration(600);
        circleHeightAnimator2.setRepeatCount(ValueAnimator.INFINITE);
        circleHeightAnimator2.setRepeatMode(ValueAnimator.REVERSE);
        circleHeightAnimator2.setInterpolator(new DecelerateInterpolator());
        //延迟160ms，等线先到最低点，再开始球的周期运动
        circleHeightAnimator2.setStartDelay(160);
        circleHeightAnimator2.start();

        //初始化角度引擎
        ObjectAnimator degreeAnimator = ObjectAnimator.ofFloat(this, "degree", 0f, -30f, 0f, 15f, 0, -10, 0, 5, 0);
        degreeAnimator.setDuration(1200);
        degreeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        degreeAnimator.setRepeatMode(ValueAnimator.RESTART);
        degreeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        degreeAnimator.start();

        degreeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                quadControllerPointF = new PointF(540, 960);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置渐变，从点A到点B线性渐变
        shader = new LinearGradient(leftRectPointF.x - 10 * factor, leftRectPointF.y, rightRectPointF.x + 10 * factor, rightRectPointF.y, leftGradientColor,
                rightGradientColor, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);
        //设置拐角圆角
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(15);
        //设置画笔为圆笔头
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

        //画点
        canvas.drawPoint(leftRectPointF.x, leftRectPointF.y, mPaint);
        canvas.drawPoint(rightRectPointF.x, rightRectPointF.y, mPaint);


        //画左右俩个旋转后的正方形
        drawLiftAndRightRect(canvas);

        //画曲线
        drawLine(canvas);

        //画球
        drawCircle(canvas);
    }

    /**
     * 画左右俩旋转45度的正方形
     */
    private void drawLiftAndRightRect(Canvas canvas) {
        //画左边正方形
        canvas.save();
        canvas.rotate(45 - degree, leftRectPointF.x, leftRectPointF.y);
        canvas.drawRoundRect(leftRectPointF.x - 7 * factor, leftRectPointF.y - 7 * factor, leftRectPointF.x + 7 * factor, leftRectPointF.y + 7 * factor, 15, 15, mPaint);
        canvas.restore();

        //画右边正方形
        canvas.save();
        //角度值因为是左右相反，和上面相反
        canvas.rotate(45 + degree, rightRectPointF.x, rightRectPointF.y);
        canvas.drawRoundRect(rightRectPointF.x - 7 * factor, rightRectPointF.y - 7 * factor, rightRectPointF.x + 7 * factor, rightRectPointF.y + 7 * factor, 15, 15, mPaint);
        canvas.restore();
    }

    /**
     * 画球
     */
    private void drawCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(circlePointF.x, circlePointF.y, 9 * factor, mPaint);
    }

    /**
     * 画曲线
     */
    private void drawLine(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(lineLeftEndPointF.x, lineLeftEndPointF.y);
        mPath.quadTo(quadControllerPointF.x, quadControllerPointF.y, lineRightEndPointF.x, lineRightEndPointF.y);
        canvas.drawPath(mPath, mPaint);

    }

    /**
     * 输入起点、长度、旋转角度计算终点
     * <p>
     * 知道一个线段，一个定点，线段旋转角度求终点坐标
     * 根据极坐标系原理 x = pcos(a), y = psin(a)
     *
     * @param startPoint 起点
     * @param length     长度
     * @param angle      旋转角度
     * @return 计算结果点
     */
    private static PointF calculatPoint(PointF startPoint, float length, float angle) {
        float deltaX = (float) Math.cos(Math.toRadians(angle)) * length;
        //符合Android坐标的y轴朝下的标准，和y轴有关的统一减180度
        float deltaY = (float) Math.sin(Math.toRadians(angle - 180)) * length;
        return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);
    }

    /**
     * 计算控制点
     */
    private PointF calculatQuadControllerPointF(float length, float degree) {

        //提高控制点高度
        length += 20 * factor;
        float height = -(float) (Math.tan(Math.toRadians(degree)) * length);
        Log.d("height", "height:" + height);
        return new PointF(mScreenWidth / 2, mScreenHeight / 2 + height);
    }

}
