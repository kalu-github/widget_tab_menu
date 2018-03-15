package lib.kalu.tabmenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;


/**
 * description: 底部导航菜单
 * created by kalu on 2017/6/18 15:03
 */
public class TabMenuLayout extends LinearLayout {

    private final String BUNDLE_PARCELABLE = "BUNDLE_PARCELABLE";
    private final Paint paint = new Paint();

    private OnTabMenuChangedListener listener;

    private int lineHeight;
    private int defaultPosition = Activity.DEFAULT_KEYS_DISABLE;

    private boolean isSwitchAlpha; // 透明度动画
    private boolean isSwitchScale; // 选中动画

    /*********************************************************************************************/

    public TabMenuLayout(Context context) {
        this(context, null);
    }

    public TabMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float scale = context.getResources().getDisplayMetrics().density;
        lineHeight = (int) (1 * scale + 0.5f);
        setPadding(0, lineHeight, 0, 0);

        TypedArray array = context.getApplicationContext().obtainStyledAttributes(attrs, R.styleable.TabMenuLayout);

        try {
            isSwitchAlpha = array.getBoolean(R.styleable.TabMenuLayout_tml_switch_alpha, false);
            isSwitchScale = array.getBoolean(R.styleable.TabMenuLayout_tml_click_scale, false);
            defaultPosition = array.getInt(R.styleable.TabMenuLayout_tml_default_position, 0);
        } catch (Exception e) {
            // LogUtil.e("TabMenuLayout", e.getMessage(), e);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.parseColor("#66e6e6e6"));
        canvas.drawRect(0, 0, getWidth(), lineHeight, paint);
    }

    /*********************************************************************************************/

    public void setViewPager(final ViewPager viewPager) {
        if (null == viewPager) {
            throw new NullPointerException("viewpager为null");
        }

        if (null == viewPager.getAdapter()) {
            throw new NullPointerException("viewpager的adapter为null");
        }
        if (viewPager.getAdapter().getCount() != getChildCount()) {
            throw new IllegalArgumentException("子View数量必须和ViewPager条目数量一致");
        }

        viewPager.setCurrentItem(defaultPosition, false);

        // 1.点击监听
        for (int i = 0; i < getChildCount(); i++) {

            final int tabPosition = i;

            View tab = getChildAt(tabPosition);
            if (null != tab && tab instanceof TabMenuView) {
                final TabMenuView tabView = (TabMenuView) tab;

                tabView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (null != listener) {
                            View sub = getChildAt(tabPosition);
                            if (null != sub && (sub instanceof TabMenuView)) {
                                setTag(true);
                                listener.onTabMenuChange(false, true, ((TabMenuView) sub).isHightLight(), tabPosition);
                            }
                        }

                        //不能使用平滑滚动，否者颜色改变会乱
                        viewPager.setCurrentItem(tabPosition, false);
                    }
                });
            } else {
                throw new IllegalArgumentException("TabMenuLayout的子View必须是TabMenuView");
            }
        }

        // 2.选中监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (positionOffset <= 0.f) return;
                Log.e("kaluff", "onPageScrolled, position = " + position + ", positionOffset = " + positionOffset);

                if (isSwitchAlpha) {
                    //滑动时的透明度动画
                    View sub = getChildAt(position);
                    if (null != sub && (sub instanceof TabMenuView)) {
                        TabMenuView menu = (TabMenuView) sub;
                        menu.setStyle(1f - positionOffset);
                    }

                    View sub2 = getChildAt(position + 1);
                    if (null != sub2 && (sub2 instanceof TabMenuView)) {
                        TabMenuView menu = (TabMenuView) sub2;
                        menu.setStyle(positionOffset);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

                for (int j = 0; j < getChildCount(); j++) {
                    Log.e("kaluff", "onPageSelected ==> position = " + position + ", index = " + j);

                    View sub = getChildAt(j);
                    if (null != sub && (sub instanceof TabMenuView)) {

                        TabMenuView menu = (TabMenuView) sub;

                        if (j == position) {
                            menu.setStyle(true, isSwitchAlpha, 1f);
                            if (isSwitchScale) {
                                menu.beginAnim();
                            }
                        } else {
                            menu.setStyle(false, isSwitchAlpha, 0f);
                            if (isSwitchScale) {
                                menu.clearAnim();
                            }
                        }
                    }
                }

                if (null != listener) {
                    Object tag = getTag();
                    if (null != tag) {
                        setTag(null);
                    } else {
                        listener.onTabMenuChange(true, false, false, position);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for (int j = 0; j < getChildCount(); j++) {

            View sub = getChildAt(j);
            if (null != sub && (sub instanceof TabMenuView)) {
                TabMenuView menu = (TabMenuView) sub;
                menu.setStyle(j == defaultPosition, isSwitchAlpha, j == defaultPosition ? 1f : 0f);
            }
        }
    }

    /*********************************************************************************************/

    public void setOnTabMenuChangedListener(OnTabMenuChangedListener listner) {
        this.listener = listner;
    }

    private void setBadgeMessage(int position, boolean isBadgePoint, int badgeNumber) {

        if (position > (getChildCount() - 1)) return;

        View sub = getChildAt(position);
        if (null != sub && (sub instanceof TabMenuView)) {
            TabMenuView menu = (TabMenuView) getChildAt(position);
            if (isBadgePoint) {
                menu.showBadgePoint();
            } else {
                menu.showBadgeNumber(badgeNumber);
            }
        }
    }

    public void setBadgeMessageBackup(int position) {

        View sub = getChildAt(position);
        if (null != sub && (sub instanceof TabMenuView)) {
            TabMenuView menu = (TabMenuView) getChildAt(position);
            setBadgeMessage(position, menu.getBadgeNumber());
        }
    }

    public void setBadgeMessage(int position, int badgeNumber) {
        setBadgeMessage(position, false, badgeNumber);
    }

    public void setBadgeMessage(int position) {
        setBadgeMessage(position, true, -1);
    }

    /**
     * 清除所有未读消息
     */
    public void removeAllBadgeMessage() {

        for (int i = 0; i < getChildCount(); i++) {

            View sub = getChildAt(i);
            if (null != sub && (sub instanceof TabMenuView)) {
                TabMenuView menu = (TabMenuView) sub;
                menu.clearBadge();
            }
        }
    }

    /**
     * 清除当前选中菜单未读消息
     */
    public void removeCurrentBadgeMessage() {

        for (int i = 0; i < getChildCount(); i++) {

            View sub = getChildAt(i);
            if (null != sub && (sub instanceof TabMenuView)) {
                TabMenuView menu = (TabMenuView) sub;
                if (menu.isHightLight()) {
                    menu.clearBadge();
                }
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_PARCELABLE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_PARCELABLE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**********************************************************************************************/

    public interface OnTabMenuChangedListener {

        /**
         * @param isSwitch        是否左右滑动
         * @param isClick         是否点击
         * @param isClickSelected 是否点击选中
         * @param position        位置标记
         */
        void onTabMenuChange(boolean isSwitch, boolean isClick, boolean isClickSelected, int position);
    }
}
