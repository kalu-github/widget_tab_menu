package lib.kalu.tabmenu;

import android.animation.ValueAnimator;
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
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * description: 底部导航菜单
 * created by kalu on 2017/6/18 15:03
 */
public final class TabMenuView extends View {

    // 画笔
    private final Paint FIN_PAINT = new Paint();   //背景的画笔
    // 图标绘制区域
    private final Rect mIconRect = new Rect();
    private final Handler mHandler = new WeakHandler(this);
    private Bitmap mIconNormal;                   //默认图标
    private Bitmap mIconSelected;                 //选中的图标
    // 文字信息
    private String mText;                         //描述文本
    private int mTextColorNormal = 0xFF999999;    //描述文本的默认显示颜色
    private int mTextColorSelected = 0xFF46C01B;  //述文本的默认选中显示颜色
    private int mTextSize = 12;                   //描述文本的默认字体大小 12sp
    private int mPadding = 10;                      //文字和图片之间的距离 5dp
    private boolean isHightLight = false; // 是否选中
    private boolean isSwitchAlpha = false;
    private float mAlpha = 0;                         //当前的透明度
    private int mBadgeBackgroundColor = 0xFFFF0000;       //默认红颜色
    // 未读消息, -1显示小圆点, >0显示消息数量(超过99显示99+)
    private int mBadgeNumber;
    // 背景颜色, 默认
    private int mBackgroundColorNormal = Color.TRANSPARENT;
    // 背景颜色, 按压
    private int mBackgroundColorPress = Color.TRANSPARENT;
    private int left1;
    private int right1;
    private int top1;
    private int bottom1;

    private float iconWidth;
    private float iconHeight;
    private float textHeight;

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
            mBackgroundColorNormal = a.getColor(R.styleable.TabMenuView_tmv_background_color_normal, mBackgroundColorNormal);
            mBackgroundColorPress = a.getColor(R.styleable.TabMenuView_tmv_background_color_press, mBackgroundColorPress);

            textHeight = a.getDimension(R.styleable.TabMenuView_tmv_text_height, 0);
            iconWidth = a.getDimension(R.styleable.TabMenuView_tmv_icon_width, 0);
            iconHeight = a.getDimension(R.styleable.TabMenuView_tmv_icon_height, 0);

        } catch (Exception e) {
        } finally {
            a.recycle();
        }

        // 按压背景色
        if (mBackgroundColorPress != Color.TRANSPARENT && mBackgroundColorNormal != Color.TRANSPARENT) {
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 初始化文字大小
        FIN_PAINT.reset();
        FIN_PAINT.clearShadowLayer();
        FIN_PAINT.setTextSize(mTextSize);

        int height = getHeight();
        int width1 = getWidth();

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
            FIN_PAINT.setTextAlign(Paint.Align.CENTER);
            final float textX = width1 / 2f;
            final float textY = (height - textHeight / 2);
            if (isSwitchAlpha) {
                FIN_PAINT.setColor(mTextColorNormal);
                FIN_PAINT.setAlpha(255 - alpha);
                canvas.drawText(mText, textX, textY, FIN_PAINT);
                FIN_PAINT.setColor(mTextColorSelected);
                FIN_PAINT.setAlpha(alpha);
                canvas.drawText(mText, textX, textY, FIN_PAINT);
                //  LogUtil.e(TAG, "文字 - 过度效果");
            } else {
                FIN_PAINT.setColor(isHightLight ? mTextColorSelected : mTextColorNormal);
                canvas.drawText(mText, textX, textY, FIN_PAINT);
                //   LogUtil.e(TAG, "文字 - 普通效果");
            }
        }

        // 2.画图标
        if (null != mIconNormal && null != mIconSelected) {

            // 1.计算真实的图标位置
            if (left1 == 0 && top1 == 0 && right1 == 0 && bottom1 == 0) {
                int c1 = width1 / 2;
                int c2 = (int) (iconWidth / 5);
                int c3 = (int) (iconHeight / 5);
                left1 = (int) (c1 - iconWidth / 2) + c2;
                top1 = c3;
                right1 = (int) (c1 + iconWidth / 2) - c2;
                bottom1 = (int) iconHeight - c3;
                mIconRect.left = left1;
                mIconRect.right = right1;
                mIconRect.top = top1;
                mIconRect.bottom = bottom1;
            }

            // 2.画
            FIN_PAINT.reset();
            FIN_PAINT.clearShadowLayer();
            FIN_PAINT.setAntiAlias(true);
            FIN_PAINT.setFilterBitmap(true);
            FIN_PAINT.setDither(true);
            FIN_PAINT.setStrokeCap(Paint.Cap.ROUND);
            FIN_PAINT.setStrokeJoin(Paint.Join.ROUND);

            // 是否类似微信icon透明度动画
            if (isSwitchAlpha) {
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
                //    LogUtil.e(TAG, "onDraw ==> 画图标, 高级");
            } else {
                //   LogUtil.e(TAG, "onDraw ==> 画图标, 普通");
                canvas.drawBitmap(isHightLight ? mIconSelected : mIconNormal, null, mIconRect, FIN_PAINT);
            }
        }

//         3.未读消息
//         显示小圆点
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
    public final void showBadgePoint() {
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
    public final void showBadgeNumber(int badgeNum) {
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
    protected final void clearBadge() {
        mBadgeNumber = 0;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public final int getBadgeNumber() {
        return mBadgeNumber;
    }

    /**
     * @param alpha 对外提供的设置透明度的方法，取值 0.0 ~ 1.0
     */
    public final void setStyle(float alpha) {
        setStyle(isHightLight, isSwitchAlpha, alpha);
    }

    protected final void setStyle(boolean isHightLight, boolean isSwitchAlpha) {
        setStyle(isHightLight, isSwitchAlpha, 0f);
    }

    protected final void setStyle(boolean isHightLight, boolean isSwitchAlpha, float alpha) {

        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("透明度必须是 0.0 - 1.0");
        }
        this.mAlpha = alpha;

        this.isHightLight = isHightLight;
        this.isSwitchAlpha = isSwitchAlpha;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 高亮显示
     */
    protected final boolean isHightLight() {
        return this.isHightLight;
    }

    private final ValueAnimator valueAnimator = new ValueAnimator();

    protected final void beginAnim() {

        if (mHandler.hasMessages(1)) return;

        //  Log.e("alu", "beginAnim");
        Message obtain = Message.obtain();
        obtain.what = 1;
        mHandler.sendMessage(obtain);
    }

    protected final void clearAnim() {


        if (mHandler.hasMessages(1)) {
            mHandler.removeCallbacksAndMessages(null);

            mIconRect.left = left1;
            mIconRect.right = right1;
            mIconRect.top = top1;
            mIconRect.bottom = bottom1;

            // Log.e("alu", "clearAnim");

            if (Looper.getMainLooper() == Looper.myLooper()) {
                invalidate();
            } else {
                postInvalidate();
            }
        }
    }

    private void todo(int count) {

        if (count <= 5) {

            //Log.e("alu", "缩小");
            final int temp1 = mIconRect.width() / 14;
            mIconRect.left = mIconRect.left + temp1;
            mIconRect.top = mIconRect.top + temp1;
            mIconRect.right = mIconRect.right - temp1;
            mIconRect.bottom = mIconRect.bottom - temp1;

            Message obtain = Message.obtain();
            obtain.what = count + 1;
            mHandler.sendMessageDelayed(obtain, 0);
        } else if (count <= 10) {

            // Log.e("alu", "放大");
            final int temp1 = mIconRect.width() / 14;
            mIconRect.left = mIconRect.left - temp1;
            mIconRect.top = mIconRect.top - temp1;
            mIconRect.right = mIconRect.right + temp1;
            mIconRect.bottom = mIconRect.bottom + temp1;

            if (mIconRect.left < left1) {
                mIconRect.left = left1;
            }

            if (mIconRect.right > right1) {
                mIconRect.right = right1;
            }

            if (mIconRect.top < top1) {
                mIconRect.top = top1;
            }

            if (mIconRect.bottom > bottom1) {
                mIconRect.bottom = bottom1;
            }

            Message obtain = Message.obtain();
            obtain.what = count + 1;
            mHandler.sendMessageDelayed(obtain, 0);
        } else {

            // Log.e("alu", "复位");
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

    private final float dp2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale);
    }

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