/**
  * Created by MomoLuaNative.
  * Copyright (c) 2020, Momo Group. All rights reserved.
  *
  * This source code is licensed under the MIT.
  * For the full copyright and license information,please view the LICENSE file in the root directory of this source tree.
  */
package com.immomo.mmui.ui;

import androidx.viewpager.widget.ViewPager;

import com.immomo.mls.fun.ui.PageIndicator;
import com.immomo.mmui.ILView;
import com.immomo.mmui.ud.UDNodeView;

/**
 * Created by XiongFangyu on 2018/9/27.
 */

public interface IViewPager<V extends UDNodeView> extends ILView<V> {
    ViewPager getViewPager();

    boolean isAutoScroll();

    void setAutoScroll(boolean autoScroll);

    boolean isRepeat();

    void setRepeat(boolean repeat);

    float getFrameInterval();

    void setFrameInterval(float frameInterval);

    void setPageIndicator(PageIndicator pageIndicator);

    PageIndicator getPageIndicator();

    void addCallback(Callback c);

    void removeCallback(Callback c);

    public interface Callback {
        void callbackEndDrag(int p);
        void callbackStartDrag(int p);
    }
}