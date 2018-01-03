package lib.kalu.tabmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;


/**
 * description: 底部导航菜单
 * created by kalu on 2017/6/18 15:03
 */
public class TabMenuLayout extends LinearLayout {

    private final ArrayList<TabMenuView> mTabViews = new ArrayList();
    private final String STATE_INSTANCE = "STATE_INSTANCE";
    private final String STATE_ITEM = "STATE_ITEM";
    private final Paint paint = new Paint();

    private ViewPager mViewPager;
    private OnTabMenuChangedListener listener;
    /**
     * 子View的数量
     */
    private int mChildCounts;
    /**
     * 当前的条目索引
     */
    private int mCurrentItem = 0;

    private int lineHeight;

    private boolean isClicked = false;
    private boolean isSwitchAlpha;
    private boolean isSwitchPager = true;

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
        setBackgroundColor(Color.WHITE);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TabMenuLayout);

        try {
            isSwitchAlpha = array.getBoolean(R.styleable.TabMenuLayout_tml_switch_alpha, false);
            isSwitchPager = array.getBoolean(R.styleable.TabMenuLayout_tml_switch_pager, false);
        } catch (Exception e) {
            // LogUtil.e("TabMenuLayout", e.getMessage(), e);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mChildCounts = getChildCount();

        for (int i = 0; i < mChildCounts; i++) {

            final int tabPosition = i;

            View tab = getChildAt(tabPosition);
            if (null != tab && tab instanceof TabMenuView) {
                final TabMenuView tabView = (TabMenuView) getChildAt(i);
                mTabViews.add(tabView);

                tabView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isSwitchPager) {
                            isClicked = true;
                        }

                        //点击前先重置所有按钮的状态
                        resetState();
                        tabView.setTag(tabView.getId(), true);
                        tabView.setIconAlpha(1.0f);
                        if (null != listener && null != mViewPager) {
                            int currentItem = mViewPager.getCurrentItem();
                            listener.onTabMenuClick(currentItem == tabPosition, tabPosition);
                        }
                        if (null != mViewPager) {
                            //不能使用平滑滚动，否者颜色改变会乱
                            mViewPager.setCurrentItem(tabPosition, false);
                        }
                    }
                });
            } else {
                throw new IllegalArgumentException("TabMenuLayout的子View必须是TabMenuView");
            }
        }

        mTabViews.get(mCurrentItem).setIconAlpha(1.0f);
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

    public void setViewPager(ViewPager mViewPager) {
        this.mViewPager = mViewPager;

        if (null != mViewPager) {

            if (null == mViewPager.getAdapter()) {
                throw new NullPointerException("viewpager的adapter为null");
            }
            if (mViewPager.getAdapter().getCount() != mChildCounts) {
                throw new IllegalArgumentException("子View数量必须和ViewPager条目数量一致");
            }

            if (isSwitchPager) {

                //对ViewPager添加监听
                mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        if (isSwitchAlpha) {
                            //滑动时的透明度动画
                            if (positionOffset > 0) {
                                mTabViews.get(position).setIconAlpha(1 - positionOffset);
                                mTabViews.get(position + 1).setIconAlpha(positionOffset);
                            }
                            //滑动时保存当前按钮索引
                            mCurrentItem = position;
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        resetState();
                        mTabViews.get(position).setIconAlpha(1.0f);
                        mCurrentItem = position;

                        if (!isClicked && null != listener) {
                            listener.onTabMenuSwitch(position);
                        }

                        isClicked = false;
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            }
        }
    }

    public void setOnTabMenuChangedListener(OnTabMenuChangedListener listner) {
        this.listener = listner;
    }

    public TabMenuView getCurrentItemView() {
        // sInit();
        return mTabViews.get(mCurrentItem);
    }

    public TabMenuView getTabView(int tabIndex) {
        // isInit();
        return mTabViews.get(tabIndex);
    }

    public void removeAllBadge() {
        // isInit();

        for (int i = 0; i < mTabViews.size(); i++) {
            TabMenuView tabMenuView = mTabViews.get(i);

            if (null != tabMenuView) {
                tabMenuView.removeShow();
            }
        }
    }

    public void setTabSelected(int tabIndex) {
        if (tabIndex < mChildCounts && tabIndex > -1) {
            mTabViews.get(tabIndex).performClick();
        } else {
            throw new IllegalArgumentException("IndexOutOfBoundsException");
        }
    }

    /**
     * 重置所有按钮的状态
     */
    private void resetState() {
        for (int i = 0; i < mChildCounts; i++) {
            mTabViews.get(i).setIconAlpha(0);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_ITEM, mCurrentItem);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentItem = bundle.getInt(STATE_ITEM);
            if (null == mTabViews || mTabViews.size() == 0) {
                super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
                return;
            }
            //重置所有按钮状态
            resetState();
            //恢复点击的条目颜色
            mTabViews.get(mCurrentItem).setIconAlpha(1.0f);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    /**********************************************************************************************/

    public interface OnTabMenuChangedListener {

        /**
         * Tab点击监听
         *
         * @param isSelected 当前是否已经被选中
         * @param position   位置标记
         */
        void onTabMenuClick(boolean isSelected, int position);

        /**
         * ViewPager切换监听
         *
         * @param position 位置标记
         */
        void onTabMenuSwitch(int position);
    }
}
