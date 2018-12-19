package jp.co.recruit_lifestyle.sample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.recruit.floatingview.R;
import jp.co.recruit_lifestyle.android.floatingview.PathMenuLayout;

/**
 * 自定义菜单
 *
 * @author 何凌波
 */
public class PathMenuCustom  {
    private PathMenuLayout mPathMenuLayout;
    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;

    private Context mContext;

    private int position;// 按钮的位置



    private FrameLayout frameLayout;
    private boolean checkParam;

    public PathMenuCustom(Context context) {
        this.mContext = context;
        init(mContext);
    }

    public PathMenuCustom(Context context, AttributeSet attrs) {
        this.mContext = context;
        init(mContext);

    }

    public void destroy() {
        if(windowManager!=null){
            windowManager.removeView(mPathMenuLayout);
        }
    }


    /**
     * Initialize the center button layout, load the layout file, set the touch event
     */
    private void init(Context context) {
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        final LayoutInflater inflater = LayoutInflater.from(context);

        frameLayout = (FrameLayout) inflater.inflate(
                R.layout.control_fab_service2, null);

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switchState();
                } catch (Exception e){
                    Log.d(getClass().getSimpleName(), e.getMessage());
                }
            }
        });


        mPathMenuLayout = (PathMenuLayout) frameLayout.findViewById(R.id.item_layout);
        mPathMenuLayout.setMinRadius((int) (60 * Resources.getSystem().getDisplayMetrics().density));
        mPathMenuLayout.setChildSize((int) (40 * Resources.getSystem().getDisplayMetrics().density));
        mPathMenuLayout.setRotateAnime(true);
        mPathMenuLayout.setOnListenAnimationEnd(new PathMenuLayout.ListenAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                if(!mPathMenuLayout.isExpanded()){
                    updatePencilParams(false);
                }
            }
        });
        ImageView item = new ImageView(context);
        item.setImageResource(R.drawable.ic_sample);
        item.setBackgroundResource(R.drawable.circle_fab_red);
        item.setPadding(24, 24, 24, 24);

        mPathMenuLayout.addView(item);
        ImageView item2 = new ImageView(context);
        item2.setImageResource(R.drawable.ic_sample);
        item2.setBackgroundResource(R.drawable.circle_fab_red);
        item2.setPadding(24, 24, 24, 24);

        mPathMenuLayout.addView(item2);
        ImageView item1 = new ImageView(context);
        item1.setBackgroundResource(R.drawable.circle_fab_red);
        item1.setPadding(24, 24, 24, 24);
        item1.setImageResource(R.drawable.ic_email);

        mPathMenuLayout.addView(item1);
//        ImageView item2 = new ImageView(context);
//        item2.setImageResource(R.drawable.ic_email);
//        mPathMenuLayout.addView(item2);
//        ImageView item3 = new ImageView(context);
//        item3.setImageResource(R.drawable.ic_email);


//        mPathMenuLayout.addView(item3);


        getParam(false);
        windowManager.addView(frameLayout, mParams);
    }

    public static int getLayoutFlag(){
        int OVERLAY_TYPE;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return OVERLAY_TYPE;
    }

    public void setPositionInWindow(int centerX, int centerY, int widthParent, int heightParent, int width, int height){
        int threadHold =  mPathMenuLayout.MIN_RADIUS;
        if(centerX < threadHold){
            if(centerY < threadHold) {
                position = PathMenuLayout.LEFT_TOP;
            } else if((heightParent -  centerY) < threadHold){
                position = PathMenuLayout.LEFT_BOTTOM;
            }else {
                position = PathMenuLayout.LEFT_CENTER;
            }
        } else if((widthParent - centerX) < threadHold) {
            if(centerY < threadHold) {
                position = PathMenuLayout.RIGHT_TOP;
            } else if((heightParent -  centerY) < threadHold){
                position = PathMenuLayout.RIGHT_BOTTOM;
            }else {
                position = PathMenuLayout.RIGHT_CENTER;
            }
        } else {
            if(centerY < threadHold) {
                position = PathMenuLayout.CENTER_TOP;
            } else if((heightParent -  centerY) < threadHold){
                position = PathMenuLayout.CENTER_BOTTOM;
            }else {
                position = PathMenuLayout.CENTER;
            }
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPathMenuLayout.getLayoutParams();

        switch (position) {
            case PathMenuLayout.LEFT_TOP://左上
                mPathMenuLayout.setArc(0, 90, position);
                break;
            case PathMenuLayout.LEFT_CENTER://左中
                mPathMenuLayout.setArc(300, 300 + 120, position);
                break;
            case PathMenuLayout.LEFT_BOTTOM://左下
                mPathMenuLayout.setArc(270, 360, position);
                break;
            case PathMenuLayout.RIGHT_TOP://右上
                mPathMenuLayout.setArc(90, 180, position);
                break;
            case PathMenuLayout.RIGHT_CENTER://右中
                mPathMenuLayout.setArc(270, 90, position);
                break;
            case PathMenuLayout.RIGHT_BOTTOM://右下
                mPathMenuLayout.setArc(180, 270, position);
                break;
            case PathMenuLayout.CENTER_TOP://上中
                mPathMenuLayout.setArc(0, 180, position);
                break;
            case PathMenuLayout.CENTER_BOTTOM://下中
                mPathMenuLayout.setArc(180, 360, position);
                break;
            case PathMenuLayout.CENTER:
                mPathMenuLayout.setArc(0, 360, position);
                break;
        }

        params.setMargins(centerX - mPathMenuLayout.centerX, centerY - mPathMenuLayout.centerY, 0, 0);
        mPathMenuLayout.setLayoutParams(params);
        mPathMenuLayout.requestLayout();
    }
    private void getParam(boolean check) {
        checkParam = check;
        if(check){
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    getLayoutFlag(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            mParams.gravity = Gravity.TOP | Gravity.START;
        } else {
            mParams = new WindowManager.LayoutParams(
                    1,
                    1,
                    getLayoutFlag(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            mParams.gravity = Gravity.TOP | Gravity.START;
        }
    }

    public void updatePencilParams(boolean check) {
        getParam(check);
        if(windowManager != null  && frameLayout != null) {
            windowManager.updateViewLayout(frameLayout, mParams);
        }
    }

    public void switchState() {
        updatePencilParams(true);
        mPathMenuLayout.post(new Runnable() {
            @Override
            public void run() {
                mPathMenuLayout.switchState(true);

            }
        });
    }
}