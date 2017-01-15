package com.codbking.calendar.exaple;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * Created by lijunguan on 2017/1/15.
 * email: lijunguan199210@gmail.com
 * blog: https://lijunguan.github.io
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
