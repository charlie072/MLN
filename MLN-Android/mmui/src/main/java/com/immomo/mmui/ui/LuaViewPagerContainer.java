/**
  * Created by MomoLuaNative.
  * Copyright (c) 2020, Momo Group. All rights reserved.
  *
  * This source code is licensed under the MIT.
  * For the full copyright and license information,please view the LICENSE file in the root directory of this source tree.
  */
package com.immomo.mmui.ui;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.immomo.mls.fun.ui.PageIndicator;
import com.immomo.mls.fun.weight.BorderRadiusFrameLayout;
import com.immomo.mls.util.LuaViewUtil;
import com.immomo.mmui.ud.UDViewPager;

/**
 * Created by zhang.ke
 * on 2019/9/30
 */
public class LuaViewPagerContainer extends BorderRadiusFrameLayout implements IViewPager<UDViewPager> {
    private UDViewPager udViewPager;
    private LuaViewPager luaViewPager;

    public LuaViewPagerContainer(@NonNull Context context, UDViewPager userdata) {
        super(context);
        udViewPager = userdata;
        luaViewPager = new LuaViewPager(context, userdata);
        addView(luaViewPager, LuaViewUtil.createRelativeLayoutParamsMM());
    }

    @Override
    public UDViewPager getUserdata() {
        return udViewPager;
    }

    @Override
    public LuaViewPager getViewPager() {
        return luaViewPager;
    }

    @Override
    public void setViewLifeCycleCallback(ViewLifeCycleCallback cycleCallback) {
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // animHelper.startIfNeed();
        luaViewPager.callOnAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // animHelper.stopAnim();
        luaViewPager.callOnDetachedFromWindow();
    }

    @Override
    public boolean isAutoScroll() {
        return luaViewPager.isAutoScroll();
    }

    @Override
    public void setAutoScroll(boolean autoScroll) {
        luaViewPager.setAutoScroll(autoScroll);
    }

    @Override
    public boolean isRepeat() {
        return luaViewPager.isRepeat();
    }

    @Override
    public void setRepeat(boolean repeat) {
        luaViewPager.setRepeat(repeat);
    }

    @Override
    public float getFrameInterval() {
        return luaViewPager.getFrameInterval();
    }

    @Override
    public void setFrameInterval(float frameInterval) {
        luaViewPager.setFrameInterval(frameInterval);
    }

    @Override
    public void setPageIndicator(PageIndicator pageIndicator) {
        luaViewPager.setPageIndicator(pageIndicator);
    }

    @Override
    public PageIndicator getPageIndicator() {
        return luaViewPager.getPageIndicator();
    }

    @Override
    public void addCallback(Callback c) {
        luaViewPager.addCallback(c);
    }

    @Override
    public void removeCallback(Callback c) {
        luaViewPager.removeCallback(c);
    }

}