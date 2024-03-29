package com.demo.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lib.kalu.tabmenu.TabMenuLayout;

public class MainActivity extends AppCompatActivity {

    private TabMenuLayout alphaTabsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager mViewPger = (ViewPager) findViewById(R.id.mViewPager);
        MainAdapter mainAdapter = new MainAdapter(getSupportFragmentManager());
        mViewPger.setAdapter(mainAdapter);
        mViewPger.addOnPageChangeListener(mainAdapter);

        alphaTabsIndicator = (TabMenuLayout) findViewById(R.id.alphaIndicator);
        alphaTabsIndicator.setViewPager(mViewPger);

        alphaTabsIndicator.setBadgeMessage(0, 6);
        alphaTabsIndicator.setBadgeMessage(1, 888);
        alphaTabsIndicator.setBadgeMessage(2, 88);
        alphaTabsIndicator.setBadgeMessage(3);

        alphaTabsIndicator.setOnTabMenuChangedListener(new TabMenuLayout.OnTabMenuChangedListener() {
            @Override
            public void onTabMenuChange(boolean isSwitch, boolean isClick, boolean isClickSelected, int position) {
                Log.e("kalu", "onTabMenuChange ==> isSwitch = " + isSwitch + ", isClick = " + isClick+ ", isClickSelected = " + isClickSelected+ ", position = " + position);
            }
        });
    }


    private class MainAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        private List<Fragment> fragments = new ArrayList<>();
        private String[] titles = {"微信", "通讯录", "发现", "我"};

        public MainAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(TextFragment.newInstance(titles[0]));
            fragments.add(TextFragment.newInstance(titles[1]));
            fragments.add(TextFragment.newInstance(titles[2]));
            fragments.add(TextFragment.newInstance(titles[3]));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (0 == position) {
                alphaTabsIndicator.setBadgeMessageBackup(0);
            } else if (2 == position) {
                alphaTabsIndicator.removeCurrentBadgeMessage();
            } else if (3 == position) {
                alphaTabsIndicator.removeAllBadgeMessage();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    public final class MeViewPager extends ViewPager {

        public MeViewPager(Context context) {
            super(context);
        }

        public MeViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent arg0) {
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent arg0) {
            return false;
        }
    }
}
