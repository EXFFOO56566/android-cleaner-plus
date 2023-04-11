package com.ferdi.cleaner.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ferdi.cleaner.adapter.ClearMemoryAdapter;
import com.ferdi.cleaner.base.BaseSwipeBackActivity;
import com.ferdi.cleaner.bean.AppProcessInfo;
import com.ferdi.cleaner.model.StorageSize;
import com.ferdi.cleaner.service.CoreService;
import com.ferdi.cleaner.utils.Constants;
import com.ferdi.cleaner.utils.StorageUtil;
import com.ferdi.cleaner.utils.SystemBarTintManager;
import com.ferdi.cleaner.utils.UIElementsHelper;
import com.ferdi.cleaner.utils.ViewUtil;
import com.ferdi.cleaner.views.HoloCircularProgressBar;
import com.ferdi.cleaner.views.RotateLoading;
import com.ferdi.cleaner.widget.textcounter.CounterView;
import com.ferdi.cleaner.widget.textcounter.formatters.DecimalFormatter;
import com.ferdi.cleanerr.R;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


public class MemoryCleanActivity extends BaseSwipeBackActivity implements  OnDismissCallback, CoreService.OnPeocessActionListener, ClearMemoryAdapter.OnItemCheckedChangeListener {

    private static final int INITIAL_DELAY_MILLIS = 300;
    public long Allmemory;
    ActionBar ab;

    @Bind(R.id.bottom_lin)
    LinearLayout bottom_lin;
    @Bind(R.id.clear_button)
    Button clearButton;
    @Bind(R.id.counterLayout)
    RelativeLayout counterLayout;
    @Bind(R.id.header)
    FrameLayout header;
    @Bind(R.id.header_selected)
    LinearLayout headerSelected;

    @Bind(R.id.layout_container)
    RelativeLayout layoutContainer1;
    @Bind(R.id.layout_container2)
    RelativeLayout layoutContainer2;

    @Bind(R.id.clean_up_tv_done)
    TextView doneBtn;
    @Bind(R.id.clean_done_iv_done)
    ImageView doneImg;

    @Bind(R.id.clean_done_holoCircularProgressBar)
    HoloCircularProgressBar doneProgressBar;


    List<AppProcessInfo> mAppProcessInfos = new ArrayList();
    ClearMemoryAdapter mClearMemoryAdapter;
    long mKillAppmemory = 0;

    @Bind(R.id.listview)
    ListView mListView;
    @Bind(R.id.progressBar)
    View mProgressBar;
    @Bind(R.id.progressBarText)
    TextView mProgressBarText;
    @Bind(R.id.rotate_loading)
    RotateLoading mRotateLoading;
  
    @Bind(R.id.sufix)
    TextView sufix;
    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter;
    @Bind(R.id.textCounter)
    CounterView textCounter;
    @Bind(R.id.textSelected)
    TextView textSelected;



    private CoreService mCoreService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MemoryCleanActivity.this.mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            MemoryCleanActivity.this.mCoreService.setOnActionListener(MemoryCleanActivity.this);
            MemoryCleanActivity.this.mCoreService.scanRunProcess();
        }

        public void onServiceDisconnected(ComponentName name) {
            MemoryCleanActivity.this.mCoreService.setOnActionListener(null);
            MemoryCleanActivity.this.mCoreService = null;
        }
    };

    AdRequest adRequest;
    AdView adView;
    InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_memory_clean);

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
        setFont();
        this.mKillAppmemory = 0;
        this.mClearMemoryAdapter = new ClearMemoryAdapter(this.mContext, this.mAppProcessInfos);
        this.mClearMemoryAdapter.SetOnItemCheckChangeListener(this);
        this.mListView.setAdapter(this.mClearMemoryAdapter);
        this.mRotateLoading.start();
        this.clearButton.setVisibility(8);
        bindService(new Intent(this.mContext, CoreService.class), this.mServiceConnection, 1);
        int footerHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.footer_height);
        this.textCounter.setAutoFormat(false);
        this.textCounter.setFormatter(new DecimalFormatter());
        this.textCounter.setAutoStart(false);
        this.textCounter.setIncrement(5.0f);
        this.textCounter.setTimeInterval(50);
        this.headerSelected.setVisibility(8);
        this.textSelected.setVisibility(8);
    }

    private void setFont() {
        ViewUtil.overrideFonts(this, this.counterLayout, Constants.FONT_HEAVY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Apply KitKat specific translucency.
     */
    private void applyKitKatTranslucency() {

        // KitKat translucent navigation/status bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            // mTintManager.setTintColor(0xF00099CC);

            mTintManager.setTintDrawable(UIElementsHelper
                    .getGeneralActionBarBackground(this));

            getActionBar().setBackgroundDrawable(
                    UIElementsHelper.getGeneralActionBarBackground(this));

        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onDismiss(@NonNull ViewGroup viewGroup, @NonNull int[] ints) {

    }

    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }



    @Override
    public void onScanProgressUpdated(Context context, int current, int max, int size) {
        this.mProgressBarText.setText(getString(R.string.scanning_m_of_n, new Object[]{Long.valueOf(current), Long.valueOf(max)}));
        this.mKillAppmemory += size;
        StorageSize mStorageSize = StorageUtil.convertStorageSize(this.mKillAppmemory);
        this.textCounter.setCurrentTextValue(mStorageSize.value);
        this.sufix.setText(mStorageSize.suffix);
    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        this.mAppProcessInfos.clear();
        StorageSize mStorageSize = StorageUtil.convertStorageSize(this.mKillAppmemory);
        this.Allmemory = 0;
        for (AppProcessInfo appInfo : apps) {
            if (!appInfo.isSystem) {
                this.mAppProcessInfos.add(appInfo);
                this.Allmemory += appInfo.memory;
            }
        }
        showProgressBar(false);
        this.clearButton.setVisibility(0);
        this.mListView.setVisibility(0);
        this.mClearMemoryAdapter.notifyDataSetChanged();
        this.headerSelected.setVisibility(0);
        this.textSelected.setVisibility(0);
        this.textSelected.setText("Selected: " + mStorageSize.value + " " + mStorageSize.suffix);
        if (apps.size() > 0) {
            this.header.setVisibility(0);
            this.bottom_lin.setVisibility(0);
            return;
        }
        this.header.setVisibility(8);
        this.bottom_lin.setVisibility(8);


    }

    private void refeshTextCounter() {
        StorageSize mStorageSize = StorageUtil.convertStorageSize(Allmemory);
        textCounter.setStartValue(0f);
        textCounter.setEndValue(mStorageSize.value);
        sufix.setText(mStorageSize.suffix);
        textCounter.start();
    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        startActivity(new Intent(this, CleanUpDone.class));

    }

    @OnClick(R.id.clean_up_tv_done)
    public void onClickDone() {

        interstitial.show();
        finish();

    }


    @OnClick(R.id.clear_button)
    public void onClickClear() {
        getActionBar().hide();
        this.layoutContainer1.setVisibility(8);
        this.layoutContainer2.setVisibility(0);
        Animation anim = AnimationUtils.loadAnimation(this.mContext, R.anim.rocket_anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                MemoryCleanActivity.this.doneImg.setImageResource(R.drawable.ic_done_white_128dp_2x);
                MemoryCleanActivity.this.doneBtn.setVisibility(0);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        this.doneImg.setAnimation(anim);
        this.doneImg.getDrawable().clearColorFilter();
        CleanMemory();


    }

    private void CleanMemory() {
        AsyncTask<Long, Float, Long> asynTask = new AsyncTask<Long, Float, Long>() {
            long killAppmemory = 0;
            long mAllMem;
            int size;

            protected Long doInBackground(Long... voids) {
                this.mAllMem = voids[0].longValue();
                this.size = MemoryCleanActivity.this.mAppProcessInfos.size();
                for (int i = MemoryCleanActivity.this.mAppProcessInfos.size() - 1; i >= 0; i--) {
                    if (( MemoryCleanActivity.this.mAppProcessInfos.get(i)).checked) {
                        this.killAppmemory = ( MemoryCleanActivity.this.mAppProcessInfos.get(i)).memory + this.killAppmemory;
                        MemoryCleanActivity.this.mCoreService.killBackgroundProcesses((MemoryCleanActivity.this.mAppProcessInfos.get(i)).processName);
                        MemoryCleanActivity.this.mAppProcessInfos.remove(MemoryCleanActivity.this.mAppProcessInfos.get(i));
                    }
                    float progress = ((float) (this.size - i)) / ((float) this.size);
                    publishProgress(new Float[]{Float.valueOf(progress)});
                }
                return Long.valueOf(this.killAppmemory);
            }

            protected void onProgressUpdate(Float... values) {
                super.onProgressUpdate(values);
                MemoryCleanActivity.this.doneProgressBar.setProgress(values[0].floatValue());
            }

            protected void onPostExecute(Long l) {
                super.onPostExecute(l);
                this.mAllMem -= l.longValue();
                if (this.mAllMem > 0) {
                    MemoryCleanActivity.this.refeshTextCounter();
                }
            }
        }.execute(new Long[]{Long.valueOf(this.Allmemory)});
    }


    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    public void OnCheckedChange(AppProcessInfo appInfo) {
        if (appInfo.checked) {
            this.mKillAppmemory += appInfo.memory;
        } else {
            this.mKillAppmemory -= appInfo.memory;
        }
        StorageSize mStorageSize = StorageUtil.convertStorageSize(this.mKillAppmemory);
        this.textSelected.setText("Selected: " + mStorageSize.value + " " + mStorageSize.suffix);
    }
}
