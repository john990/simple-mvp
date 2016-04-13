package com.john990.mvp.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.john990.mvp.R;
import com.john990.mvp.contract.BasePresenter;
import com.john990.mvp.contract.BaseView;
import com.john990.mvp.utils.CommonHandler;
import com.john990.mvp.utils.SystemBarTintManager;

/**
 * Created by John on 16/4/10.
 */
public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements CommonHandler.MessageHandler, BaseView<T> {
    private static final String TAG = "BaseActivity";
    public static final int WINDOW_TYPE_NORMAL = 0;
    public static final int WINDOW_TYPE_NO_TITLE = 2;
    private Toolbar toolbar;
    private FrameLayout contentView;

    private CommonHandler handler = new CommonHandler(this);
    private static int isForeground = 0;

    protected T presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = createPresenter();
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(layoutResID, WINDOW_TYPE_NORMAL);
    }

    public void setContentView(int layoutResID, int windowType) {
        super.setContentView(R.layout.activity_base);
        if (windowType == WINDOW_TYPE_NORMAL) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            contentView = (FrameLayout) findViewById(R.id.activity_base_view_container);
            LayoutInflater.from(this).inflate(layoutResID, contentView);
            initFrame();
        } else if (windowType == WINDOW_TYPE_NO_TITLE) {
            contentView = (FrameLayout) findViewById(android.R.id.content);
            contentView.removeAllViews();
            LayoutInflater.from(this).inflate(layoutResID, contentView);
            initFrame();
        }
    }

    private void initFrame() {
        showStatusBar();
    }

    private void showStatusBar() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);

            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setNavigationBarTintEnabled(true);
            systemBarTintManager.setTintColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public void setStatusBarColor(int realColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(realColor);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setNavigationBarTintEnabled(true);
            systemBarTintManager.setTintColor(realColor);
        }
    }

    public void fullScreen() {
        hideToolBar();
    }

    public int getContentHeight() {
        return contentView.getHeight();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void hideToolBar() {
        toolbar.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        params.topMargin = 0;
        contentView.setLayoutParams(params);
    }

    public void setToolBarColor(int rsid) {
        toolbar.setBackgroundResource(rsid);
    }

    public void showTitleBack() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setTitle(int resId) {
        getSupportActionBar().setTitle(resId);
    }

    public void setTitle(CharSequence text) {
        getSupportActionBar().setTitle(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void finishDelay(long time) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, time);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void handleMessage(Message msg) {

    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelay(Runnable runnable, long time) {
        handler.postDelayed(runnable, time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground++;
    }

    public abstract String pageName();

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeground--;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static boolean isForeground() {
        return isForeground > 0;
    }
}
