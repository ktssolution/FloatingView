package jp.co.recruit_lifestyle.sample.fragment;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import jp.co.recruit.floatingview.R;

/**
 * FloatingViewの設定を行います。
 */
public class FloatingViewSettingsFragment extends PreferenceFragment {

    /**
     * FloatingViewSettingsFragmentを生成します。
     *
     * @return FloatingViewSettingsFragment
     */
    public static FloatingViewSettingsFragment newInstance() {
        final FloatingViewSettingsFragment fragment = new FloatingViewSettingsFragment();
        return fragment;
    }

    /**
     * コンストラクタ
     */
    public FloatingViewSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_floatingview);
    }
}
