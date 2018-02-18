[ ![Download](https://api.bintray.com/packages/zhanghang/maven/tabmenulayout/images/download.svg) ](https://bintray.com/zhanghang/maven/tabmenulayout/_latestVersion) ![](https://img.shields.io/badge/Build-Passing-green.svg) ![](https://img.shields.io/badge/API%20-14+-green.svg) [ ![](https://img.shields.io/badge/%E4%BD%9C%E8%80%85-%E5%BC%A0%E8%88%AA-red.svg) ](http://www.jianshu.com/u/22a5d2ee8385) ![](https://img.shields.io/badge/%E9%82%AE%E7%AE%B1-153437803@qq.com-red.svg)
```
compile 'lib.kalu.tabmenu:tabmenulayout:<latest-version>'
```

 [戳我下载 ==>](https://pan.baidu.com/s/1hueqEeK)

![image](https://github.com/153437803/TabMenuLayout/blob/master/Screenrecorder-2018-01-04.gif )
![image](https://github.com/153437803/TabMenuLayout/blob/master/Screenrecorder-2018-02-19.gif )
![image](https://github.com/153437803/TabMenuLayout/blob/master/Screenrecorder-2018-02-20.gif )

# 适用场景：
```
1.点击选中菜单, 列表回滚到顶部, 功能已实现
2.菜单切换, 图片缩小放大动画, 功能已实现（类是淘宝）
3.左右滑动, 底部菜单颜色渐变, 功能已实现
```

# 自定义属性：
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


# Proguard-Rules
```
-keep class lib.kalu.tabmenu.** {
*;
}
```

#

# License
```
Copyright 2017 张航

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
