package com.xiaozi.androidble;

import android.app.Activity;
import android.os.Handler;

/**
 * Created by xiaoz on 2017-05-25.
 */

public class BaseActivity extends Activity {
    protected final String LOG_TAG = getClass().getSimpleName();
    protected Activity mActivity = this;
    protected Handler mHandler = new Handler();
}
