package com.codbking.calendar.exaple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.calendar_agenda.CalendarPickerView;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);

        final CalendarPickerView calendarPickerView = (CalendarPickerView) findViewById(R.id.calendar_view);
        calendarPickerView.addOnPage(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle(calendarPickerView.getYearMonthTitle());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick({R.id.text1, R.id.text2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text1:
                startActivity(new Intent(MainActivity.this, XiaomiActivity.class));
                break;
            case R.id.text2:
                startActivity(new Intent(MainActivity.this, DingdingActivity.class));
                break;
        }

    }
}
