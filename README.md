![image](https://github.com/153437803/TabMenuLayout/blob/master/Screenrecorder-2018-01-04.gif )

```
<declare-styleable name="TabMenuLayout">
    <!-- 滑动变化透明度 -->
    <attr name="tml_switch_alpha" format="boolean" />
    <!-- 点击变化大小 -->
    <attr name="tml_click_scale" format="boolean" />
</declare-styleable>

<declare-styleable name="TabMenuView">
    <!-- 默认图片 -->
    <attr name="tmv_icon_normal" format="reference" />
    <!-- 选中图片 -->
    <attr name="tmv_icon_selected" format="reference" />
    <!-- 菜单文字 -->
    <attr name="tmv_text" format="string|reference" />
    <!-- 菜单文字大小 -->
    <attr name="tmv_text_size" format="dimension|reference" />
    <!-- 菜单文字默认颜色 -->
    <attr name="tmv_text_color_normal" format="color|reference" />
    <!-- 菜单文字选种颜色 -->
    <attr name="tmv_text_color_selected" format="color|reference" />
    <!-- 未读信息背景颜色 -->
    <attr name="tmv_badge_color_background" format="color|reference" />
    <!-- 文字和图片之间的距离 -->
    <attr name="tmv_text_padding_icon" format="dimension|reference" />
    <!-- 是否使用系统水波纹背景 -->
    <attr name="tmv_background_selector_system" format="boolean" />
</declare-styleable>
```
