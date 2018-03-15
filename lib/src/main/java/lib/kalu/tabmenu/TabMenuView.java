package lib.kalu.tabmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.ref.WeakReference;

/**
 * description: 底部导航菜单
 * created by kalu on 2017/6/18 15:03
 */
public class TabMenuView extends View {

    private Bitmap mIconNormal;                   //默认图标
    private Bitmap mIconSelected;                 //选中的图标

    // 文字信息
    private String mText;                         //描述文本
    private int mTextColorNormal = 0xFF999999;    //描述文本的默认显示颜色
    private int mTextColorSelected = 0xFF46C01B;  //述文本的默认选中显示颜色
    private int mTextSize = 12;                   //描述文本的默认字体大小 12sp
    private int mPadding = 10;                      //文字和图片之间的距离 5dp

    private float mAlpha;                         //当前的透明度

    private int mBadgeBackgroundColor = 0xFFFF0000;       //默认红颜色

    // 未读消息, -1显示小圆点, >0显示消息数量(超过99显示99+)
    private int mBadgeNumber;

    private boolean isUseSystemSelectorBg;

    // 画笔
    private final Paint FIN_PAINT = new Paint();   //背景的画笔
    // 文字绘制区域
    private final Rect mTextBound = new Rect();
    // 图标绘制区域
    private final Rect mIconRect = new Rect();
    // 背景颜色, 默认
    private int mBackgroundColorNormal = 0xFFFFFFFF;
    // 背景颜色, 按压
    private int mBackgroundColorPress = 0xFFFFFFFF;

    private int left1;
    private int right1;
    private int top1;
    private int bottom1;

    /***************************************************************************************/

    public TabMenuView(Context context) {
        this(context, null);
    }

    public TabMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, getResources().getDisplayMetrics());
        mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mPadding, getResources().getDisplayMetrics());

        //获取所有的自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabMenuView);
        try {
            BitmapDrawable iconNormal = (BitmapDrawable) a.getDrawable(R.styleable.TabMenuView_tmv_icon_normal);
            if (iconNormal != null) {
                mIconNormal = iconNormal.getBitmap();
            }
            BitmapDrawable iconSelected = (BitmapDrawable) a.getDrawable(R.styleable.TabMenuView_tmv_icon_selected);
            if (iconSelected != null) {
                mIconSelected = iconSelected.getBitmap();
            }

            if (null != mIconNormal) {
                mIconSelected = null == mIconSelected ? mIconNormal : mIconSelected;
            } else {
                mIconNormal = null == mIconSelected ? mIconNormal : mIconSelected;
            }

            mText = a.getString(R.styleable.TabMenuView_tmv_text);
            mTextSize = a.getDimensionPixelSize(R.styleable.TabMenuView_tmv_text_size, mTextSize);
            mTextColorNormal = a.getColor(R.styleable.TabMenuView_tmv_text_color_normal, mTextColorNormal);
            mTextColorSelected = a.getColor(R.styleable.TabMenuView_tmv_text_color_selected, mTextColorSelected);
            mBadgeBackgroundColor = a.getColor(R.styleable.TabMenuView_tmv_badge_color_background, mBadgeBackgroundColor);
            mPadding = (int) a.getDimension(R.styleable.TabMenuView_tmv_text_padding_icon, mPadding);
            isUseSystemSelectorBg = a.getBoolean(R.styleable.TabMenuView_tmv_background_selector_system, false);

            mBackgroundColorNormal = a.getColor(R.styleable.TabMenuView_tmv_background_color_normal, mBackgroundColorNormal);
            mBackgroundColorPress = a.getColor(R.styleable.TabMenuView_tmv_background_color_press, mBackgroundColorPress);
        } catch (Exception e) {
        } finally {
            a.recycle();
        }

        if (isUseSystemSelectorBg) {
            int[] temp = new int[]{android.R.attr.selectableItemBackground};
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(temp);
            Drawable drawable = typedArray.getDrawable(0);
            a.recycle();
            setBackgroundDrawable(drawable);
        } else {
            // 按压背景色
            StateListDrawable drawable = new StateListDrawable();
            ColorDrawable colorDrawable1 = new ColorDrawable();
            colorDrawable1.setColor(mBackgroundColorPress);
            drawable.addState(new int[]{android.R.attr.state_focused}, colorDrawable1);
            drawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed}, colorDrawable1);
            ColorDrawable colorDrawable2 = new ColorDrawable();
            colorDrawable2.setColor(mBackgroundColorNormal);
            drawable.addState(new int[0], colorDrawable2);
            setBackgroundDrawable(drawable);
        }
    }

    /***************************************************************************************/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mText == null && (mIconNormal == null || mIconSelected == null)) {
            throw new IllegalArgumentException("必须设置 tabText 或者 tabIconSelected、tabIconNormal 两个，或者全部设置");
        }

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        //计算出可用绘图的区域
        int realWidth = measuredWidth - paddingLeft - paddingRight;
        int realHeight = measuredHeight - paddingTop - paddingBottom;

        // 1.文字, 计算文字的绘图区域
        if (!TextUtils.isEmpty(mText) && null == mIconNormal) {
            int textLeft = paddingLeft + (realWidth - mTextBound.width()) / 2;
            int textTop = paddingTop + (realHeight - mTextBound.height()) / 2;
            mTextBound.set(textLeft, textTop, textLeft + mTextBound.width(), textTop + mTextBound.height());
        }
        // 2.图标, 计算出图标可以绘制的画布大小
        else if (TextUtils.isEmpty(mText) && null != mIconNormal) {
            mIconRect.set(paddingLeft, paddingTop, paddingLeft + realWidth, paddingTop + realHeight);
        }
        // 3. 文字+图标
        else {
            // 初始化文字大小
            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setTextSize(mTextSize);

            // 1.计算文字的绘图区域
            final Paint.FontMetricsInt fontMetricsInt = FIN_PAINT.getFontMetricsInt();
            int fontHeight = (fontMetricsInt.bottom - fontMetricsInt.top) / 2;
            int textLeft = paddingLeft;
            int textTop = measuredHeight - paddingBottom - fontHeight;
            int textRight = measuredWidth - paddingRight;
            int textBottom = measuredHeight - paddingBottom;
            mTextBound.set(textLeft, textTop, textRight, textBottom);

            // 2.计算出图标可以绘制的画布大小
            realHeight -= (mTextBound.height() + mPadding);
            mIconRect.set(paddingLeft, paddingTop, paddingLeft + realWidth, paddingTop + realHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Log.e("kalu6", "onDraw ==> id = " + getClass().getCanonicalName());

        final int alpha = (int) Math.ceil(mAlpha * 255);

        // 1.画文字
        if (!TextUtils.isEmpty(mText)) {
            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setTextSize(mTextSize);
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setFakeBoldText(true);
            //绘制原始文字,setAlpha必须放在paint的属性最后设置，否则不起作用
            FIN_PAINT.setColor(mTextColorNormal);
            FIN_PAINT.setAlpha(255 - alpha);
            FIN_PAINT.setTextAlign(Paint.Align.CENTER);

            final int textX = mTextBound.centerX();
            final int textY = mTextBound.bottom - mTextBound.height() * 1 / 3;

            //由于在该方法中，y轴坐标代表的是baseLine的值，经测试，mTextBound.height() + mFmi.bottom 就是字体的高
            //所以在最后绘制前，修正偏移量，将文字向上修正 mFmi.bottom / 2 即可实现垂直居中
            canvas.drawText(mText, textX, textY, FIN_PAINT);
            // Log.e("kalu44444", mTextBound.left + ", " + mTextBound.top + ", " + mTextBound.right + ", " + mTextBound.bottom);

            //绘制变色文字，setAlpha必须放在paint的属性最后设置，否则不起作用
            FIN_PAINT.setColor(mTextColorSelected);
            FIN_PAINT.setAlpha(alpha);
            canvas.drawText(mText, textX, textY, FIN_PAINT);
            // Log.e("kalu6", "onDraw ==> 画文字");
        }

        // 2.画图标
        if (null != mIconNormal && null != mIconSelected) {

            // 1.计算真实的图标位置
            if (left1 == 0) {
                float dx = 0, dy = 0;
                float wRatio = mIconRect.width() * 1.0f / mIconNormal.getWidth();
                float hRatio = mIconRect.height() * 1.0f / mIconNormal.getHeight();
                if (wRatio > hRatio) {
                    dx = (mIconRect.width() - hRatio * mIconNormal.getWidth()) / 2;
                } else {
                    dy = (mIconRect.height() - wRatio * mIconNormal.getHeight()) / 2;
                }
                final int left = (int) (mIconRect.left + dx + 0.5f);
                final int top = (int) (mIconRect.top + dy + 0.5f);
                final int right = (int) (mIconRect.right - dx + 0.5f);
                final int bottom = (int) (mIconRect.bottom - dy + 0.5f);
                mIconRect.set(left, top, right, bottom);

                left1 = left;
                top1 = top;
                right1 = right;
                bottom1 = bottom;
            }

            // 2.画
            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setAlpha(255 - alpha);
            canvas.drawBitmap(mIconNormal, null, mIconRect, FIN_PAINT);
            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setAlpha(alpha); //setAlpha必须放在paint的属性最后设置，否则不起作用
            canvas.drawBitmap(mIconSelected, null, mIconRect, FIN_PAINT);
            // Log.e("kalu6", "onDraw ==> 画图标");
        }

        // 3.未读消息
        // 显示小圆点
        if (mBadgeNumber == -1) {

            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setColor(mBadgeBackgroundColor);
            float left = getMeasuredWidth() / 10 * 6f;
            float top = dp2px(getContext(), 5);

            int i = getMeasuredWidth() / 14;
            int j = getMeasuredHeight() / 9;
            i = i >= j ? j : i;
            i = i > 10 ? 10 : i;
            float width = dp2px(getContext(), i);
            RectF messageRectF = new RectF(left, top, left + width, top + width);
            canvas.drawOval(messageRectF, FIN_PAINT);
            // Log.e("kalu6", "onDraw ==> 画小圆点");
        }
        // 显示消息数量(超过99显示99+)
        else if (mBadgeNumber > 0) {
            final Context context = getContext().getApplicationContext();

            int i = getMeasuredWidth() / 14;
            int j = getMeasuredHeight() / 9;
            i = i >= j ? j : i;

            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setColor(mBadgeBackgroundColor);
            String number = mBadgeNumber > 99 ? "99+" : String.valueOf(mBadgeNumber);
            float textSize = i / 1.5f == 0 ? 5 : i / 1.5f;
            int width;
            int hight = (int) dp2px(context, i);
            Bitmap bitmap;
            if (number.length() == 1) {
                width = (int) dp2px(context, i);
                bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
            } else if (number.length() == 2) {
                width = (int) dp2px(context, i + 5);
                bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
            } else {
                width = (int) dp2px(context, i + 8);
                bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            }
            Canvas canvasMessages = new Canvas(bitmap);
            RectF messageRectF = new RectF(0, 0, width, hight);
            canvasMessages.drawRoundRect(messageRectF, 50, 50, FIN_PAINT); //画椭圆

            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);
            FIN_PAINT.setColor(Color.WHITE);
            FIN_PAINT.setTextSize(dp2px(context, textSize));
            FIN_PAINT.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = FIN_PAINT.getFontMetrics();
            float x = width / 2f;
            float y = hight / 2f - fontMetrics.descent + (fontMetrics.descent - fontMetrics.ascent) / 2;
            canvasMessages.drawText(number, x, y, FIN_PAINT);
            float left = getMeasuredWidth() / 10 * 6f;
            float top = dp2px(context, 5);
            canvas.drawBitmap(bitmap, left, top, null);
            bitmap.recycle();
            // Log.e("kalu6", "onDraw ==> 画消息数量");
        }
    }

    /**
     * 显示小圆点
     */
    public void showBadgePoint() {
        mBadgeNumber = -1;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 显示未读消息
     *
     * @param badgeNum
     */
    public void showBadgeNumber(int badgeNum) {
        mBadgeNumber = badgeNum;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 清除消息
     */
    protected void clearBadge() {
        mBadgeNumber = 0;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public int getBadgeNumber() {
        return mBadgeNumber;
    }

    /**
     * @param alpha 对外提供的设置透明度的方法，取值 0.0 ~ 1.0
     */
    public void setIconAlpha(float alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("透明度必须是 0.0 - 1.0");
        }
        mAlpha = alpha;

        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    private final float dp2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale);
    }

    protected void beginAnim() {

        if (mHandler.hasMessages(1)) return;

        Log.e("alu", "beginAnim");
        Message obtain = Message.obtain();
        obtain.what = 1;
        mHandler.sendMessage(obtain);
    }

    protected void clearAnim() {

        if (mHandler.hasMessages(1)) {
            //mHandler.removeCallbacksAndMessages(null);

            mIconRect.left = left1;
            mIconRect.right = right1;
            mIconRect.top = top1;
            mIconRect.bottom = bottom1;

            Log.e("alu", "clearAnim");

            if (Looper.getMainLooper() == Looper.myLooper()) {
                invalidate();
            } else {
                postInvalidate();
            }
        }
    }

    private void todo(int count) {

        if (count <= 5) {

            Log.e("alu", "缩小");
            final int temp1 = mIconRect.width() / 14;
            mIconRect.left = mIconRect.left + temp1;
            mIconRect.top = mIconRect.top + temp1;
            mIconRect.right = mIconRect.right - temp1;
            mIconRect.bottom = mIconRect.bottom - temp1;

            Message obtain = Message.obtain();
            obtain.what = count + 1;
            mHandler.sendMessageDelayed(obtain, 5);
        } else if (count <= 10) {

            Log.e("alu", "放大");
            final int temp1 = mIconRect.width() / 14;
            mIconRect.left = mIconRect.left - temp1;
            mIconRect.top = mIconRect.top - temp1;
            mIconRect.right = mIconRect.right + temp1;
            mIconRect.bottom = mIconRect.bottom + temp1;

            Message obtain = Message.obtain();
            obtain.what = count + 1;
            mHandler.sendMessageDelayed(obtain, 5);
        } else {

            Log.e("alu", "复位");
            mIconRect.left = left1;
            mIconRect.right = right1;
            mIconRect.top = top1;
            mIconRect.bottom = bottom1;
        }

        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }

        //  Log.e("kalu44", "count = " + count + ", Repeat = " + mIconAvailableRect.left + " --- " + mIconAvailableRect.right + " --- " + System.currentTimeMillis());
    }

    private final Handler mHandler = new WeakHandler(this);

    private static class WeakHandler extends Handler {

        private final WeakReference<TabMenuView> mWeak;

        public WeakHandler(TabMenuView view) {
            mWeak = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            TabMenuView tab = mWeak.get();
            if (null == tab) return;

            mWeak.get().todo(msg.what);
        }
    }
}
