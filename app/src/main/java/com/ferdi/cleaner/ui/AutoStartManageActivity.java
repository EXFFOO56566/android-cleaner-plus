package com.ferdi.cleaner.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ferdi.cleaner.base.BaseSwipeBackActivity;
import com.ferdi.cleaner.fragment.AutoStartFragment;
import com.ferdi.cleaner.fragment.WeakFragmentPagerAdapter;
import com.ferdi.cleaner.utils.SystemBarTintManager;
import com.ferdi.cleaner.utils.UIElementsHelper;
import com.ferdi.cleaner.views.SlidingTab;
import com.ferdi.cleanerr.R;

import butterknife.Bind;

public class AutoStartManageActivity extends BaseSwipeBackActivity {
    ActionBar ab;
    private AdView adView;
    private MyPagerAdapter adapter;

    @Bind(R.id.pagerFragmentTask)
    ViewPager pager;
    Resources res;
    @Bind(R.id.tabs)
    SlidingTab tabs;

    public class MyPagerAdapter extends WeakFragmentPagerAdapter {
        private final String[] TITLES = new String[]{"Popular App", "System App"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public CharSequence getPageTitle(int position) {
            return this.TITLES[position];
        }

        public int getCount() {
            return this.TITLES.length;
        }

        public Fragment getItem(int position) {
            AutoStartFragment fragment = new AutoStartFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            fragment.setArguments(bundle);
            saveFragment(fragment);
            return fragment;
        }
    }

    AdRequest adRequest;
    InterstitialAd interstitial;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autostart_manage);

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
        getActionBar().setHomeButtonEnabled(true);
        this.res = getResources();
        this.adapter = new MyPagerAdapter(getSupportFragmentManager());
        this.pager.setAdapter(this.adapter);
        this.pager.setPageMargin((int) TypedValue.applyDimension(1, 4.0f, getResources().getDisplayMetrics()));
        this.tabs.setViewPager(this.pager);
        setTabsValue();
    }

    private void setTabsValue() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        this.tabs.setShouldExpand(true);
        this.tabs.setDividerColor(0);
        this.tabs.setUnderlineHeight((int) TypedValue.applyDimension(1, 1.0f, dm));
        this.tabs.setIndicatorHeight((int) TypedValue.applyDimension(1, 3.0f, dm));
        this.tabs.setTextSize((int) TypedValue.applyDimension(2, 16.0f, dm));
        this.tabs.setTextColor(Color.parseColor("#ffffff"));
        this.tabs.setIndicatorColor(Color.parseColor("#ffffff"));
        this.tabs.setSelectedTextColor(Color.parseColor("#ffffff"));
        this.tabs.setTabBackground(0);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        interstitial.show();
        finish();
        return true;
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
}
