# CustomViewSet
自定义VIew实现各种炫酷动画的集合

简书版地址：[Android自定义View动画--一个绳子拉动的弹弹球](http://www.jianshu.com/p/37e1c4655bb7)

![运行效果](http://upload-images.jianshu.io/upload_images/3515789-6f0cc2e2ec51b08d.gif?imageMogr2/auto-orient/strip)


#灵感来源
![原版](http://upload-images.jianshu.io/upload_images/3515789-bc922826116f423c.gif?imageMogr2/auto-orient/strip)


----

#分析运动规律


![平行时.jpg](http://upload-images.jianshu.io/upload_images/3515789-03250c426b361829.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![最低点.jpg](http://upload-images.jianshu.io/upload_images/3515789-b48cb6fb0ceb12c2.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![最高点.jpg](http://upload-images.jianshu.io/upload_images/3515789-31e886884c4d0503.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这是三个状态的截图

---

####组成
- 左右俩个圆点
- 俩个圆角的正方形
- 一条曲线
- 一个圆球

---

####运动规律

**1.颜色渐变：**

状态| 左正方形中点颜色值 | 线中点颜色值|右正方形中点颜色值
-|-|-|-
最高|#E42A34  |#FF8ED0|#D867DF
平行 | #E72F2C  | #FD76C1|#C118D9
最低 | #EF2539  | #F1358A|#BD19D3

**2.运动状态：**
- 俩个正方形中点是静止不变的
- 正方形和线：正方形来回上下旋转，线是向下弯接着向上弯，可以看到一开始线从水平往下拉的过程，速度是先块后慢的（动画可以选用先加速后减速的插值器```AccelerateDecelerateInterpolator```）
- 球：由图可以看出，它是1.最低到最高减速，2.最高到水平加速，3.水平到最低减速（例子里简化了该状态，用了匀速上下运动替代了整个过程）

---

#关键代码

**1.设置画笔颜色渐变和初始化画笔**

```
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
```

AB俩点指的正方形（旋转后45度后的）左右俩个端点，leftRectPointF和rightRectPointF看下面2中代码端注释

**2. 画圆角正方形并旋转45度（右边正方形为135度）**

```
    /**
     * 正方形在动画里旋转的角度
     */
    private float degree = 0;
     /**
     * 左正方形中点，屏幕中点到左右正方形中心点的距离是45*factor
     */
    private PointF leftRectPointF = new PointF(mScreenWidth / 2 - 45 * factor, mScreenHeight / 2);
    /**
     * 右正方形中点
     */
    private PointF rightRectPointF = new PointF(mScreenWidth / 2 + 45 * factor, mScreenHeight / 2);
    /**
     * 画左右俩旋转45度的正方形
     */
    private void drawLiftAndRightRect(Canvas canvas) {
        //画左边正方形
        canvas.save();
        //这里的旋转要放在最上面，因为canvas的变换是反着来的，这里需要的是先画出正方形再旋转画布
        canvas.rotate(45 - degree, leftRectPointF.x, leftRectPointF.y);
        //正方形的边长为14*factor，一半也就是7*factor
        canvas.drawRoundRect(leftRectPointF.x - 7 * factor, leftRectPointF.y - 7 * factor, leftRectPointF.x + 7 * factor, leftRectPointF.y + 7 * factor, 15, 15, mPaint);
        canvas.restore();

        //画右边正方形
        canvas.save();
        //角度值因为是左右相反，和上面相反
        canvas.rotate(45 + degree, rightRectPointF.x, rightRectPointF.y);
        canvas.drawRoundRect(rightRectPointF.x - 7 * factor, rightRectPointF.y - 7 * factor, rightRectPointF.x + 7 * factor, rightRectPointF.y + 7 * factor, 15, 15, mPaint);
        canvas.restore();
    }
```

**3.画贝塞尔曲线**

示例曲线运动规律大致如下

![3.gif](http://upload-images.jianshu.io/upload_images/3515789-fe897924f23f8b96.gif?imageMogr2/auto-orient/strip)

> 左端点右端点不变，然后不断变化控制点高度即可。

本示例的弹弹球的是左右端点上下变换，控制点也上下变化的。

```
     /**
     * 画曲线
     */
    private void drawLine(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(lineLeftEndPointF.x, lineLeftEndPointF.y);
        mPath.quadTo(quadControllerPointF.x, quadControllerPointF.y, lineRightEndPointF.x, lineRightEndPointF.y);
        canvas.drawPath(mPath, mPaint);
    }
```

用degree的角度来计算线与正方形接点的坐标

```
public void setDegree(float degree) {
        this.degree = degree;
        //算出左边正方形和线连接点坐标
        lineLeftEndPointF = calculatPoint(leftRectPointF, rectDiagonalHalf, degree);
        //右边角度的是180的（余）数
        lineRightEndPointF = calculatPoint(rightRectPointF, rectDiagonalHalf, 180 - degree);
        //控制点
        quadControllerPointF = calculatQuadControllerPointF(45 * factor, degree);

        ...

        invalidate();
    }
```

其中calculatPoint函数和calculatQuadControllerPointF如下

```
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
```
calculatPoint()是根据极坐标定律，知道起点和线段长度和旋转角度计算端点坐标

```
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
```
本来贝塞尔曲线的控制点应该是在曲线中点连线上，但是我试了一下效果，觉得曲线不够弯，所以给它加了20*factor


**4.画球**

```
/**
     * 画球
     */
    private void drawCircle(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(circlePointF.x, circlePointF.y, 9 * factor, mPaint);
    }
```

**5.```degree```数值变化引擎**

```
        //初始化角度引擎
        ObjectAnimator degreeAnimator = ObjectAnimator.ofFloat(this, "degree", 0f, -30f, 0f, 15f, 0, -10, 0, 5, 0);
        degreeAnimator.setDuration(1200);
        degreeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        degreeAnimator.setRepeatMode(ValueAnimator.RESTART);
        degreeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        degreeAnimator.start();
```

用了属性动画，设置加速减速插值器，模拟绳子反弹力越来越小的效果

**6.颜色渐变引擎**

```
      //左边渐变边界点颜色值的变化引擎
        ObjectAnimator leftGradientColorAnimator = ObjectAnimator.ofArgb(this, "leftGradientColor", 0xFFE42A34, 0xFFE72F2C, 0xFFEF2539);
        leftGradientColorAnimator.setDuration(1200);
        leftGradientColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        leftGradientColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        leftGradientColorAnimator.setInterpolator(new LinearInterpolator());
        leftGradientColorAnimator.start();
        //右边渐变边界点颜色值的变化引擎
        ObjectAnimator rightGradientColorAnimator = ObjectAnimator.ofArgb(this, "rightGradientColor", 0xFFD767DF, 0xFFC118D9, 0xFFBD19D3);
        rightGradientColorAnimator.setDuration(1200);
        rightGradientColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rightGradientColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rightGradientColorAnimator.setInterpolator(new LinearInterpolator());
        rightGradientColorAnimator.start();
```

**7.球的动画引擎**

```
      //最低往返最高
        final ObjectAnimator circleHeightAnimator2 = ObjectAnimator.ofFloat(this, "circleBottomHeight", 960 + 20 * factor, 960 - 70 * factor);
        circleHeightAnimator2.setDuration(600);
        circleHeightAnimator2.setRepeatCount(ValueAnimator.INFINITE);
        circleHeightAnimator2.setRepeatMode(ValueAnimator.REVERSE);
        circleHeightAnimator2.setInterpolator(new DecelerateInterpolator());
        //延迟160ms，等线先到最低点，再开始球的周期运动
        circleHeightAnimator2.setStartDelay(200);
        circleHeightAnimator2.start();
```

这里circleBottomHeight定义的是球的底部的y轴坐标值，球中心坐标在setDegree（）里实时算出

```
     public void setDegree(float degree) {
        ...
        
        //球的半径是10*factor
        circlePointF = new PointF(540f, circleBottomHeight - 10 * factor);
        ...
    }
```

**8.其他的看源代码吧....**

---


#不足
- 球和绳子的互动不是很和谐（因为简化了球的运动步骤）

---

#源代码:
[CustomViewSet](https://github.com/minminaya/CustomViewSet)

---

end
