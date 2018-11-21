package jp.co.recruit_lifestyle.sample.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import jp.co.recruit.floatingview.R;
import jp.co.recruit_lifestyle.android.floatingview.FloatingView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;
import jp.co.recruit_lifestyle.sample.PathMenuCustom;


/**
 * ChatHead Service
 */
public class ChatHeadService extends Service implements FloatingViewListener {

    /**
     * デバッグログ用のタグ
     */
    private static final String TAG = "ChatHeadService";

    /**
     * Prefs Key(Last position X)
     */
    private static final String PREF_KEY_LAST_POSITION_X = "last_position_x";

    /**
     * Prefs Key(Last position Y)
     */
    private static final String PREF_KEY_LAST_POSITION_Y = "last_position_y";

    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 9083150;

    /**
     * FloatingViewManager
     */
    private FloatingViewManager mFloatingViewManager;
    private WindowManager windowManager;

    private DisplayMetrics metrics;
    private ImageView iconView;
    private PathMenuCustom pathMenuCustom;

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 既にManagerが存在していたら何もしない
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }

        metrics = new DisplayMetrics();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setBackgroundResource(R.drawable.circle_fab_red);
        iconView.setPadding(24, 24, 24, 24);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, getString(R.string.chathead_click_message));
            }
        });


        pathMenuCustom = new PathMenuCustom(this);

        final FloatingViewManager.Options options = loadOptions(metrics);

        mFloatingViewManager = new FloatingViewManager(this, this, options);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        loadDynamicOptions();

        mFloatingViewManager.addViewToWindow(iconView);

        // 常駐起動
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    /**
     * 動的に変更可能なオプションを読み込みます。
     */
    private void loadDynamicOptions() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        final String displayModeSettings = sharedPref.getString("settings_display_mode", "");
        if ("Always".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        } else if ("FullScreen".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN);
        } else if ("Hide".equals(displayModeSettings)) {
            mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_ALWAYS);
        }

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


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        destroy();
        try {
            pathMenuCustom.destroy();
            pathMenuCustom = null;
        } catch (Exception e){
            Log.d(getClass().getSimpleName(), e.getMessage());
        }
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
     * FloatingViewのオプションを読み込みます。
     *
     * @param metrics X/Y座標の設定に利用するDisplayMetrics
     * @return Options
     */
    private FloatingViewManager.Options loadOptions(DisplayMetrics metrics) {
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        options.removeTrashView = false;
        // Shape
        final String shapeSettings = sharedPref.getString("settings_shape", "");
        if ("Circle".equals(shapeSettings)) {
            options.shape = FloatingViewManager.SHAPE_CIRCLE;
        } else if ("Rectangle".equals(shapeSettings)) {
            options.shape = FloatingViewManager.SHAPE_RECTANGLE;
        }

        // Margin
        final String marginSettings = sharedPref.getString("settings_margin", String.valueOf(options.overMargin));
        options.overMargin = Integer.parseInt(marginSettings) * 3;

        // MoveDirection
        final String moveDirectionSettings = sharedPref.getString("settings_move_direction", "");
        if ("Default".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_DEFAULT;
        } else if ("Left".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_LEFT;
        } else if ("Right".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_RIGHT;
        } else if ("Nearest".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_NEAREST;
        } else if ("Fix".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_NONE;
        } else if ("Thrown".equals(moveDirectionSettings)) {
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN;
        }

        options.usePhysics = sharedPref.getBoolean("settings_use_physics", true);

        // Last position
        final boolean isUseLastPosition = sharedPref.getBoolean("settings_save_last_position", false);
        if (isUseLastPosition) {
            final int defaultX = options.floatingViewX;
            final int defaultY = options.floatingViewY;
            options.floatingViewX = sharedPref.getInt(PREF_KEY_LAST_POSITION_X, defaultX);
            options.floatingViewY = sharedPref.getInt(PREF_KEY_LAST_POSITION_Y, defaultY);
        } else {
            // Init X/Y
            final String initXSettings = sharedPref.getString("settings_init_x", "");
            final String initYSettings = sharedPref.getString("settings_init_y", "");
            if (!TextUtils.isEmpty(initXSettings) && !TextUtils.isEmpty(initYSettings)) {
                final int offset = (int) (48 + 8 * metrics.density);
                options.floatingViewX = (int) (metrics.widthPixels * Float.parseFloat(initXSettings) - offset);
                options.floatingViewY = (int) (metrics.heightPixels * Float.parseFloat(initYSettings) - offset);
            }
        }

        // Initial Animation
        final boolean animationSettings = sharedPref.getBoolean("settings_animation", options.animateInitialMove);
        options.animateInitialMove = animationSettings;

        return options;
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
    public void onFinished(FloatingView floatingView, int x, int y) {

        // convert toa do xy cua left bottom -> left top
        pathMenuCustom.setPositionInWindow(
                x + floatingView.getWidth() /2,
                (floatingView.getViewParentHeight() - y) - floatingView.getHeight()/2,
                floatingView.getViewParentWidth(), floatingView.getViewParentHeight(),
                floatingView.getWidth(), floatingView.getHeight());
    }

    @Override
    public void onClick() {
        Log.d(TAG, "onClick");
        pathMenuCustom.switchState();

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
//        Log.d(TAG, "onTouch");

    }

    /**
     * Viewを破棄します。
     */
    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
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
