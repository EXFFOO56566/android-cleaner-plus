package com.ferdi.cleaner.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ferdi.cleaner.adapter.RublishMemoryAdapter;
import com.ferdi.cleaner.base.BaseSwipeBackActivity;
import com.ferdi.cleaner.model.CacheListItem;
import com.ferdi.cleaner.model.StorageSize;
import com.ferdi.cleaner.service.CleanerService;
import com.ferdi.cleaner.utils.StorageUtil;
import com.ferdi.cleaner.utils.SystemBarTintManager;
import com.ferdi.cleaner.utils.UIElementsHelper;
import com.ferdi.cleaner.views.RotateLoading;
import com.ferdi.cleaner.widget.textcounter.CounterView;
import com.ferdi.cleanerr.R;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class RubbishCleanActivity extends BaseSwipeBackActivity implements CleanerService.OnActionListener, OnDismissCallback {
    private static final int INITIAL_DELAY_MILLIS = 300;
    protected static final int PROCESS_MAX = 8;
    protected static final int PROCESS_PROCESS = 9;
    protected static final int SCANING = 5;
    protected static final int SCAN_FINIFSH = 6;
    ActionBar ab;

    @Bind(R.id.bottom_lin)
    LinearLayout bottom_lin;
    @Bind(R.id.clear_button)
    Button clearButton;
    @Bind(R.id.clean_up_tv_done)
    TextView doneBtn;

    @Bind(R.id.clean_up_rocket)
    ImageView doneImg;



    @Bind(R.id.clean_done_iv_done)
    ImageView imageRush;

    @Bind(R.id.clean_done_iv_bg)
    ImageView doneRocket;

    @Bind(R.id.header)
    RelativeLayout header;
    @Bind(R.id.layout_container)
    RelativeLayout layoutContainer1;
    @Bind(R.id.layout_container2)
    RelativeLayout layoutContainer2;

    private boolean mAlreadyCleaned = false;
    private boolean mAlreadyScanned = false;
    List<CacheListItem> mCacheListItem = new ArrayList();
    long mCleanSize;
    private CleanerService mCleanerService;

    @Bind(R.id.listview)
    ListView mListView;
    @Bind(R.id.progressBar)
    View mProgressBar;
    @Bind(R.id.progressBarText)
    TextView mProgressBarText;
    @Bind(R.id.rotate_loading)
    RotateLoading mRotateLoading;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            RubbishCleanActivity.this.mCleanerService = ((CleanerService.CleanerServiceBinder) service).getService();
            RubbishCleanActivity.this.mCleanerService.setOnActionListener(RubbishCleanActivity.this);
            if (!RubbishCleanActivity.this.mCleanerService.isScanning() && !RubbishCleanActivity.this.mAlreadyScanned) {
                RubbishCleanActivity.this.mCleanerService.scanCache();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            RubbishCleanActivity.this.mCleanerService.setOnActionListener(null);
            RubbishCleanActivity.this.mCleanerService = null;
        }
    };
    int pprocess = 0;
    int ptotal = 0;
    Resources res;
    RublishMemoryAdapter rublishMemoryAdapter;
    @Bind(R.id.sufix)
    TextView sufix;
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;
    @Bind(R.id.textCounter)
    CounterView textCounter;

    AdRequest adRequest;
    AdView adView;
    InterstitialAd interstitial;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rublish_clean);

        adRequest = new AdRequest.Builder().build();

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getResources().getString(R.string.inter));
        interstitial.loadAd(adRequest);

        adView = (AdView) findViewById(R.id.adsView);

        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.res = getResources();
        this.mRotateLoading.start();
        int footerHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        this.rublishMemoryAdapter = new RublishMemoryAdapter(this.mContext, this.mCacheListItem);
        this.mListView.setAdapter(this.rublishMemoryAdapter);
        this.mListView.setOnItemClickListener(this.rublishMemoryAdapter);
        bindService(new Intent(this.mContext, CleanerService.class), this.mServiceConnection, 1);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {
    }

    public void onScanStarted(Context context) {
        this.mProgressBarText.setText(R.string.scanning);
        this.mCleanSize = 0;
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max, int size) {
        this.mProgressBarText.setText(getString(R.string.scanning_m_of_n, new Object[]{Long.valueOf(current), Long.valueOf(max)}));
        this.mCleanSize += size;
        StorageSize mStorageSize = StorageUtil.convertStorageSize(this.mCleanSize);
        this.textCounter.setCurrentTextValue(mStorageSize.value);
        this.sufix.setText(mStorageSize.suffix);
    }



    public void onScanCompleted(Context context, List<CacheListItem> apps) {
        showProgressBar(false);
        this.mCacheListItem.clear();
        this.mCacheListItem.addAll(apps);
        this.rublishMemoryAdapter.notifyDataSetChanged();
        this.bottom_lin.setVisibility(0);
        if (!this.mAlreadyScanned) {
            this.mAlreadyScanned = true;
        }
        this.mListView.setVisibility(0);
    }

    public void onCleanStarted(Context context) {
        if (isProgressBarVisible()) {
            showProgressBar(false);
        }
        if (!isFinishing()) {
            showDialogLoading();
        }
    }

    public void onCleanCompleted(Context context, long cacheSize) {
        dismissDialogLoading();
        this.header.setVisibility(View.GONE);
        this.bottom_lin.setVisibility(View.GONE);
        this.mCacheListItem.clear();
        this.rublishMemoryAdapter.notifyDataSetChanged();
    }

    private void applyKitKatTranslucency() {
        if (VERSION.SDK_INT >= 19) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            mTintManager.setTintDrawable(UIElementsHelper.getGeneralActionBarBackground(this));
            getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(this));
        }
    }

    @OnClick({R.id.clean_up_tv_done})
    public void onClickDone() {
        interstitial.show();
        finish();

    }

    @OnClick(R.id.clear_button)
    public void onClickClear() {
        getActionBar().hide();
        this.layoutContainer1.setVisibility(View.GONE);
        this.layoutContainer2.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(this.mContext, R.anim.rote_anim);
        anim.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                RubbishCleanActivity.this.doneRocket.setVisibility(View.GONE);
                RubbishCleanActivity.this.doneImg.setImageResource(R.drawable.ic_done_white_128dp_2x);
                imageRush.setVisibility(View.GONE);
                RubbishCleanActivity.this.doneBtn.setVisibility(0);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        this.doneRocket.setAnimation(anim);
        if (this.mCleanerService != null && !this.mCleanerService.isScanning() && !this.mCleanerService.isCleaning() && this.mCleanerService.getCacheSize() > 0) {
            this.mAlreadyCleaned = false;
            this.mCleanerService.cleanCache();
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= 67108864;
        } else {
            winParams.flags &= -67108865;
        }
        win.setAttributes(winParams);
    }

    private boolean isProgressBarVisible() {
        return this.mProgressBar.getVisibility() == 0;
    }

    private void showProgressBar(boolean show) {
        if (show) {
            this.mProgressBar.setVisibility(0);
            return;
        }
        this.mProgressBar.startAnimation(AnimationUtils.loadAnimation(this.mContext, 17432577));
        this.mProgressBar.setVisibility(View.GONE);
    }

    public void onDestroy() {
        unbindService(this.mServiceConnection);
        super.onDestroy();
    }
}
