package com.march.slidingchecksample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.march.baselib.ui.activity.BaseActivity;

/**
 * Project  : QuickRv
 * Package  : com.march.quickrv
 * CreateAt : 16/9/1
 * Describe :
 *
 * @author chendong
 */
public abstract class MyBaseActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected String[] getPermission2Check() {
        return new String[0];
    }

    @Override
    protected boolean isInitTitle() {
        return false;
    }
}
