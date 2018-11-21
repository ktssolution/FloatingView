package jp.co.recruit_lifestyle.android.floatingview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

/**
 * 子菜单项布局
 *
 * @author 何凌波
 */
public class PathMenuLayout extends ViewGroup {
    private int mChildSize; // 子菜单项大小相同
    private int mChildPadding = 5;
    public static final float DEFAULT_FROM_DEGREES = 270.0f;
    public static final float DEFAULT_TO_DEGREES = 360.0f;
    private float mFromDegrees = DEFAULT_FROM_DEGREES;
    private float mToDegrees = DEFAULT_TO_DEGREES;
    public static final int MIN_RADIUS = 200;
    private int mRadius;// 中心菜单圆点到子菜单中心的距离
    private boolean mExpanded = false;
    private boolean rotateAnime = false;

    public static final int LEFT_TOP = 1;
    public static final int CENTER_TOP = 2;
    public static final int RIGHT_TOP = 3;
    public static final int LEFT_CENTER = 4;
    public static final int CENTER = 5;
    public static final int RIGHT_CENTER = 6;
    public static final int LEFT_BOTTOM = 7;
    public static final int CENTER_BOTTOM = 8;
    public static final int RIGHT_BOTTOM = 9;
    private int position = LEFT_TOP;
    public int centerX = 0;
    public int centerY = 0;
    private ListenAnimationEnd listenAnimationEnd;

    public void computeCenterXY(int position) {
        computeCenterXY(position, getWidth(), getHeight());
    }
    public void computeCenterXY(int position, int width, int height) {
        switch (position) {
            case LEFT_TOP://左上
                centerX = width / 2 - getRadiusAndPadding();
                centerY = height / 2 - getRadiusAndPadding();
                break;
            case LEFT_CENTER://左中
                centerX = width / 2 - getRadiusAndPadding();
                centerY = height / 2;
                break;
            case LEFT_BOTTOM://左下
                centerX = width / 2 - getRadiusAndPadding();
                centerY = height / 2 + getRadiusAndPadding();
                break;
            case CENTER_TOP://上中
                centerX = width / 2;
                centerY = height / 2 - getRadiusAndPadding();
                break;
            case CENTER_BOTTOM://下中
                centerX = width / 2;
                centerY = height / 2 + getRadiusAndPadding();
                break;
            case RIGHT_TOP://右上
                centerX = width / 2 + getRadiusAndPadding();
                centerY = height / 2 - getRadiusAndPadding();
                break;
            case RIGHT_CENTER://右中
                centerX = width / 2 + getRadiusAndPadding();
                centerY = height / 2;
                break;
            case RIGHT_BOTTOM://右下
                centerX = width / 2 + getRadiusAndPadding();
                centerY = height / 2 + getRadiusAndPadding();
                break;

            case CENTER:
                centerX = width / 2;
                centerY = height / 2;
                break;
        }
    }

    private int getRadiusAndPadding() {
        return mRadius + (mChildPadding * 2);
    }

    public PathMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildSize((int) (44 * Resources.getSystem().getDisplayMetrics().density));
    }


    /**
     * 计算半径
     */
    private static int computeRadius(final float arcDegrees,
                                     final int childCount, final int childSize, final int childPadding,
                                     final int minRadius) {
        if (childCount < 2) {
            return minRadius;
        }
//        final float perDegrees = arcDegrees / (childCount - 1);


        final float perDegrees = arcDegrees == 360 ? (arcDegrees) / (childCount) : (arcDegrees) / (childCount - 1);


        final float perHalfDegrees = perDegrees / 2;
        final int perSize = childSize + childPadding;

        final int radius = (int) ((perSize / 2) / Math.sin(Math
                .toRadians(perHalfDegrees)));

        return Math.max(radius, minRadius);
    }

    /**
     * 计算子菜单项的范围
     */
    private static Rect computeChildFrame(final int centerX, final int centerY,
                                          final int radius, final float degrees, final int size) {
        //子菜单项中心点
        final double childCenterX = centerX + radius
                * Math.cos(Math.toRadians(degrees));
        final double childCenterY = centerY + radius
                * Math.sin(Math.toRadians(degrees));
        //子菜单项的左上角，右上角，左下角，右下角
        return new Rect((int) (childCenterX - size / 2),
                (int) (childCenterY - size / 2),
                (int) (childCenterX + size / 2),
                (int) (childCenterY + size / 2));
    }

    public int getRadius() {
        return mRadius;
    }


    /**
     * 子菜单项大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int size = getSize();
        setMeasuredDimension(size, size);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i)
                    .measure(
                            MeasureSpec.makeMeasureSpec(mChildSize,
                                    MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(mChildSize,
                                    MeasureSpec.EXACTLY));
        }
    }

    public int getSize() {
        int radius = mRadius = computeRadius(
                Math.abs(mToDegrees - mFromDegrees), getChildCount(),
                mChildSize, mChildPadding, MIN_RADIUS);
        int layoutPadding = mChildPadding * 2;
        int size = radius * 2 + mChildSize + mChildPadding
                + layoutPadding * 2;
        return size;
    }

    /**
     * 子菜单项位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        final int centerX = getWidth() / 2 - mRadius;
//        final int centerY = getHeight() / 2;
        computeCenterXY(position);
        //当子菜单要收缩时radius=0，在ViewGroup坐标中心
        final int radius = mExpanded ? mRadius : 0;

        final int childCount = getChildCount();
//        final float perDegrees =Math.abs (mToDegrees - mFromDegrees) / (childCount - 1);
        final float perDegrees = Math.abs(mToDegrees - mFromDegrees) == 360 ? (Math.abs(mToDegrees - mFromDegrees)) / (childCount) : (Math.abs(mToDegrees - mFromDegrees)) / (childCount - 1);


        float degrees = mFromDegrees;
        for (int i = 0; i < childCount; i++) {
            Rect frame = computeChildFrame(centerX, centerY, radius, degrees,
                    mChildSize);
            degrees += perDegrees;
            getChildAt(i).layout(frame.left, frame.top, frame.right,
                    frame.bottom);
        }
    }

    /**
     * 计算动画开始时的偏移量
     */
    private static long computeStartOffset(final int childCount,
                                           final boolean expanded, final int index, final float delayPercent,
                                           final long duration, Interpolator interpolator) {
        final float delay = delayPercent * duration;
        final long viewDelay = (long) (getTransformedIndex(expanded,
                childCount, index) * delay);
        final float totalDelay = delay * childCount;

        float normalizedDelay = viewDelay / totalDelay;
        normalizedDelay = interpolator.getInterpolation(normalizedDelay);

        return (long) (normalizedDelay * totalDelay);
    }

    /**
     * 变换时的子菜单项索引
     */
    private static int getTransformedIndex(final boolean expanded,
                                           final int count, final int index) {
        if (expanded) {
            return count - 1 - index;
        }

        return index;
    }

    /**
     * 展开动画
     */
    private static Animation createExpandAnimation(float fromXDelta,
                                                   float toXDelta, float fromYDelta, float toYDelta, long startOffset,
                                                   long duration, Interpolator interpolator, boolean rotateAnime) {
        Animation animation = new RotateAndTranslateAnimation(0, toXDelta, 0,
                toYDelta, 0, rotateAnime ? 360 : 0);
        animation.setStartOffset(startOffset);
        animation.setDuration(duration);
        animation.setInterpolator(interpolator);
        animation.setFillAfter(true);

        return animation;
    }

    /**
     * 收缩动画
     */
    private static Animation createShrinkAnimation(float fromXDelta,
                                                   float toXDelta, float fromYDelta, float toYDelta, long startOffset,
                                                   long duration, Interpolator interpolator, boolean rotateAnime) {
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.setFillAfter(true);
        //收缩过程中，child 逆时针自旋转360度
        final long preDuration = duration / 2;
        Animation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateAnimation.setStartOffset(startOffset);
        rotateAnimation.setDuration(preDuration);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setFillAfter(true);

//        animationSet.addAnimation(rotateAnimation);
        //Displacement during contraction and rotate 360 ​​degrees counterclockwise
        Animation translateAnimation = new RotateAndTranslateAnimation(0,
                toXDelta, 0, toYDelta, 0, rotateAnime ? 360 : 0);
        translateAnimation.setStartOffset(startOffset + preDuration);
        translateAnimation.setDuration(duration - preDuration);
        translateAnimation.setInterpolator(interpolator);
        translateAnimation.setFillAfter(true);

        animationSet.addAnimation(translateAnimation);

        return animationSet;
    }

    /**
     * 绑定子菜单项动画
     */
    private void bindChildAnimation(final View child, final int index,
                                    final long duration) {
        final boolean expanded = mExpanded;
//        final int centerX = getWidth() / 2 - mRadius;  //ViewGroup的中心X坐标
//        final int centerY = getHeight() / 2;

        computeCenterXY(position);
        final int radius = expanded ? 0 : mRadius;

        final int childCount = getChildCount();
        final float perDegrees = Math.abs(mToDegrees - mFromDegrees) == 360 ? (mToDegrees - mFromDegrees) / (childCount) : (mToDegrees - mFromDegrees) / (childCount - 1);
        Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees
                + index * perDegrees, mChildSize);

        final int toXDelta = frame.left - child.getLeft();//Expand or shrink the animation, child displacement distance along the X axis
        final int toYDelta = frame.top - child.getTop();//Expand or shrink the animation, child displacement distance along the Y axis

        Interpolator interpolator = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//            interpolator = mExpanded ? new AccelerateInterpolator()
//                    : new OvershootInterpolator(1.5f);
//        }
        if(mExpanded) {
            interpolator = new AccelerateInterpolator();
        } else {
            interpolator = new OvershootInterpolator(1.5f);
        }


        final long startOffset = computeStartOffset(childCount, mExpanded,
                index, 0.1f, duration, interpolator);

        //mExpanded is true, expanded, shrinking animation; false, expanding animation
        Animation animation = mExpanded ? createShrinkAnimation(0, toXDelta, 0,
                toYDelta, startOffset, duration, interpolator, rotateAnime)
                : createExpandAnimation(0, toXDelta, 0, toYDelta, startOffset,
                duration, interpolator, rotateAnime);

        final boolean isLast = getTransformedIndex(expanded, childCount, index) == childCount - 1;
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isLast) {
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onAllAnimationsEnd();
                        }
                    }, 0);
                }
            }
        });

        child.setAnimation(animation);
    }

    public boolean isExpanded() {
        return mExpanded;
    }


    /**
     * 设定弧度
     */
    public void setArc(float fromDegrees, float toDegrees, int position) {
        this.position = position;
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return;
        }

        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        int size = getSize();
        computeCenterXY(position, size, size);
        requestLayout();
    }

    /**
     * 设定弧度
     */
    public void setArc(float fromDegrees, float toDegrees) {
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return;
        }

        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        int size = getSize();
        computeCenterXY(position, size, size);
        requestLayout();
    }

    /**
     * 设定子菜单项大小
     */
    public void setChildSize(int size) {
        if (mChildSize == size || size < 0) {
            return;
        }

        mChildSize = size;

        requestLayout();

    }

    public int getChildSize() {
        return mChildSize;
    }

    public PathMenuLayout setChildPadding(int size){
        mChildPadding =  size;
        return this;
    }

    /**
     * 切换中心按钮的展开缩小
     */
    public void switchState(final boolean showAnimation, int position) {
        this.position = position;
        if (showAnimation) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                bindChildAnimation(getChildAt(i), i, 300);
            }
        }

        mExpanded = !mExpanded;

        if (!showAnimation) {
            requestLayout();
        }

        invalidate();
    }


    /**
     * 切换中心按钮的展开缩小
     */
    public void switchState(final boolean showAnimation) {
        if (showAnimation) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                bindChildAnimation(getChildAt(i), i, 300);
            }
        }

        mExpanded = !mExpanded;

        if (!showAnimation) {
            requestLayout();
        }

        invalidate();
    }


    /**
     * 结束所有动画
     */
    private void onAllAnimationsEnd() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).clearAnimation();
        }

        requestLayout();
        if(listenAnimationEnd!=null){
            listenAnimationEnd.onAnimationEnd();
        }

    }

    public void setRotateAnime(boolean rotateAnime) {
        this.rotateAnime = rotateAnime;
    }

    public interface ListenAnimationEnd{
        void onAnimationEnd();
    }
    public void setOnListenAnimationEnd(ListenAnimationEnd listenAnimationEnd){
        this.listenAnimationEnd = listenAnimationEnd;
    }
}