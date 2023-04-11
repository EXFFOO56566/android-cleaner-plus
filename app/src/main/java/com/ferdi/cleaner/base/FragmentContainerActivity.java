package com.ferdi.cleaner.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.ferdi.cleaner.utils.FragmentArgs;
import com.ferdi.cleanerr.R;

public class FragmentContainerActivity extends BaseSwipeBackActivity {
    public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
        Intent intent = new Intent(activity, FragmentContainerActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null) {
            intent.putExtra("args", args);
        }
        activity.startActivity(intent);
    }

    public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args, int requestCode) {
        if (fragment.getActivity() != null) {
            Intent intent = new Intent(fragment.getActivity(), FragmentContainerActivity.class);
            intent.putExtra("className", clazz.getName());
            if (args != null) {
                intent.putExtra("args", args);
            }
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        String className = getIntent().getStringExtra("className");
        if (TextUtils.isEmpty(className)) {
            finish();
            return;
        }
        FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");
        Fragment fragment = null;
        if (savedInstanceState == null) {
            try {
                Class clazz = Class.forName(className);
                fragment = (Fragment) clazz.newInstance();
                if (values != null) {
                    try {
                        clazz.getMethod("setArguments", new Class[]{Bundle.class}).invoke(fragment, new Object[]{FragmentArgs.transToBundle(values)});
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                finish();
                return;
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_fragment_container);
        if (fragment != null) {
            getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, className).commit();
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
