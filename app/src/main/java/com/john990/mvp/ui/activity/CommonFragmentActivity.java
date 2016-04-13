package com.john990.mvp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.john990.mvp.R;
import com.john990.mvp.contract.BasePresenter;
import com.john990.mvp.ui.fragment.BaseFragment;
import com.john990.mvp.utils.Hog;

public class CommonFragmentActivity<T extends BasePresenter> extends BaseActivity {
    private BaseFragment fragment;
    public static final String EXTRA_NO_TITLE = "extra.not_title";
    public static final String EXTRA_TITLE = "extra.title";
    public static final String EXTRA_FRAGMENT = "extra.fragment";
    public static final String EXTRA_PARAM = "extra.param";

    public static Intent createIntent(Context context, int title, Class<? extends BaseFragment> fragCls, Bundle param) {
        return createIntent(context, context.getString(title), fragCls, param);
    }

    public static Intent createIntent(Context context, String title, Class<? extends BaseFragment> fragCls, Bundle param) {
        return createIntent(context, title, fragCls, param, WINDOW_TYPE_NORMAL);
    }

    public static Intent createIntent(Context context, String title, Class<? extends BaseFragment> fragCls, Bundle param, int windowType) {
        Intent intent = new Intent(context, CommonFragmentActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_FRAGMENT, fragCls);
        intent.putExtra(EXTRA_NO_TITLE, windowType);
        if (param != null) {
            intent.putExtra(EXTRA_PARAM, param);
        }
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int windowType = getIntent().getIntExtra(EXTRA_NO_TITLE, WINDOW_TYPE_NORMAL);
        setContentView(R.layout.activity_common_fragment, windowType);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        Class<? extends Fragment> fragCls = (Class<? extends BaseFragment>) getIntent().getSerializableExtra(
                EXTRA_FRAGMENT);
        String tag = fragCls.getName();
        loadFragment(tag, fragCls);
        if (WINDOW_TYPE_NORMAL == windowType) {
            setTitle(title);
            showTitleBack();
        }
    }

    @Override
    public T createPresenter() {
        return null;
    }

    protected void loadFragment(String tag, Class<? extends Fragment> clazz) {
        fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            try {
                fragment = (BaseFragment) clazz.newInstance();
            } catch (Exception e) {
                Hog.e(e);
                return;
            }
        }
        Bundle param = getIntent().getBundleExtra(EXTRA_PARAM);
        if (param == null) {
            param = new Bundle();
        }
        if (fragment.getArguments() != null) {
            fragment.getArguments().clear();
            fragment.getArguments().putAll(param);
        } else {
            fragment.setArguments(param);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (fragment != null) {
            fragment.onNewIntent(intent);
        }
    }

    @Override
    public String pageName() {
        return null;
    }

}
