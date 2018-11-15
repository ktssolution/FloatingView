package jp.co.recruit_lifestyle.sample.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.recruit.floatingview.R;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;
import jp.co.recruit_lifestyle.android.floatingview.animation.enumerators.AnimationType;
import jp.co.recruit_lifestyle.android.floatingview.floatingmenubutton.FloatingMenuButton;
import jp.co.recruit_lifestyle.android.floatingview.floatingmenubutton.MovementStyle;


/**
 * ChatHead Service
 */
public class ChatHeadService extends Service implements FloatingViewListener {

    /**
     * デバッグログ用のタグ
     */
    private static final String TAG = "ChatHeadService";

    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 9083150;

    /**
     * FloatingViewManager
     */
    private FloatingViewManager mFloatingViewManager;
    private FloatingMenuButton fab_1;
    private WindowManager windowManager;
    private FrameLayout frameLayout;

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 既にManagerが存在していたら何もしない
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final ImageView iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, getString(R.string.chathead_click_message));
            }
        });


        frameLayout = (FrameLayout) inflater.inflate(
                R.layout.control_fab_service, null);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        };
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;
        // 左下の座標を0とする
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        windowManager.addView(frameLayout, mParams);
        fab_1 = frameLayout.findViewById(R.id.fab_1);
        fab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(getClass().getSimpleName(), "click");
            }
        });
        fab_1.setStartAngle(0)
                .setEndAngle(360)
                .setRadius(200)
                .setAnimationType(AnimationType.EXPAND)
                .setMovementStyle(MovementStyle.STICKED_TO_SIDES);

        fab_1.getAnimationHandler()
                .setOpeningAnimationDuration(500)
                .setClosingAnimationDuration(200)
                .setLagBetweenItems(0)
                .setOpeningInterpolator(new FastOutSlowInInterpolator())
                .setClosingInterpolator(new FastOutLinearInInterpolator())
                .shouldFade(true)
                .shouldScale(true)
                .shouldRotate(false);
        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.addViewToWindow(iconView);

        // 常駐起動
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
        Log.d(TAG, getString(R.string.finish_deleted));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {
        if (isFinishing) {
            Log.d(TAG, getString(R.string.deleted_soon));
        } else {
            Log.d(TAG, getString(R.string.touch_finished_position, x, y));
        }
    }

    @Override
    public void onClick() {
        Log.d(TAG, "onClick");
        fab_1.openMenu();
    }

    @Override
    public void onLongClick() {
        Log.d(TAG, "onLongClick");

    }

    @Override
    public void onDoubleClick() {
        Log.d(TAG, "onDoubleClick");

    }

    @Override
    public void onTouch(MotionEvent e) {
        Log.d(TAG, "onTouch");

    }

    /**
     * Viewを破棄します。
     */
    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            windowManager.removeView(frameLayout);
            mFloatingViewManager = null;
        }
    }

    /**
     * 通知を表示します。
     * クリック時のアクションはありません。
     */
    private static Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.default_floatingview_channel_id));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.chathead_content_title));
        builder.setContentText(context.getString(R.string.content_text));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }
}
