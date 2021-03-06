/**
  * Created by MomoLuaNative.
  * Copyright (c) 2019, Momo Group. All rights reserved.
  *
  * This source code is licensed under the MIT.
  * For the full copyright and license information,please view the LICENSE file in the root directory of this source tree.
  */
package com.immomo.mls.log;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.immomo.mls.util.AndroidUtil;

import java.util.Arrays;


/**
 * Created by XiongFangyu on 2018/9/6.
 */
public class DefaultPrinter extends RecyclerView implements IPrinter {
    public static int MAX_LINES = 100;
    private int maxLines = MAX_LINES;
    private A adapter;
    private int len = 0;
    private Data[] data;

    public DefaultPrinter(Context context) {
        super(context);
        setItemAnimator(null);
        data = new Data[maxLines];
        adapter = new A();
        setLayoutManager(new LinearLayoutManager(context));
        setAdapter(adapter);
        int w = AndroidUtil.getScreenWidth(context);
        int h = AndroidUtil.getScreenHeight(context);
        setLayoutParams(new ViewGroup.LayoutParams(w / 2, h / 3));
        setBackgroundColor(0x99444444);
    }

    @Override
    public void print(String s) {
        writeLastData(s);
    }

    @Override
    public void println() {
        newLine();
    }

    @Override
    public void error(String s) {
        if (len <= 1) {
            addNormalError(s);
            return;
        }
        Data pre = data[len -2];
        if (pre.type == 0) {
            addNormalError(s);
            return;
        }
        if (pre.type == -1 && s.equals(pre.getSB())) {
            pre.count ++;
            adapter.notifyItemChanged(len - 2);
            return;
        }

        if (s.equals(pre.getSB())) {
            Data last = getLastSB();
            last.type = -1;
            last.sb = data[len - 2].sb;
            last.count = 1;
            adapter.notifyItemChanged(len - 1);
            newLine();
            return;
        }

        addNormalError(s);
    }

    @Override
    public void clear() {
        Arrays.fill(data, null);
        len = 0;
        adapter.notifyDataSetChanged();
    }

    private void addNormalError(String s) {
        Data last = getLastSB();
        last.type = 1;
        last.append(s);
        adapter.notifyItemChanged(len - 1);
        newLine();
    }

    private String getPreError() {
        if (len <= 1)
            return null;
        Data last = data[len - 2];
        if (last.type == 0)
            return null;
        if (last.type == -1) {
            return last.getSB();
        }
        return last.toString();
    }

    private Data getLastSB() {
        if (len == 0) {
            len = 1;
        }
        Data last = data[len - 1];
        if (last == null) {
            last = new Data();
            data[len - 1] = last;
        }
        return last;
    }

    private void writeLastData(String s) {
        Data last = getLastSB();
        last.append(s);
        adapter.notifyItemChanged(len - 1);
        scrollToPosition(len - 1);
    }

    private void newLine() {
        len ++;
        if (len > maxLines) {
            Data[] temp = new Data[maxLines];
            int half = (maxLines >> 1) - 1;
            len = maxLines - half - 1;
            System.arraycopy(data, half, temp, 0, len);
            data = temp;
            data[len] = new Data();
            adapter.notifyDataSetChanged();
            scrollToPosition(len - 1);
            return;
        }
        data[len - 1] = new Data();
        adapter.notifyItemInserted(len - 1);
        scrollToPosition(len - 1);
    }

    public void setMaxLines(int maxLines) {
        if (this.maxLines != maxLines) {
            this.maxLines = maxLines;
            data = new Data[maxLines];
            adapter.notifyDataSetChanged();
            len = 0;
        }
    }

    private final class A extends RecyclerView.Adapter<VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(14);
            tv.setPadding(5,5,5,5);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Data d = data[position];
            final int color;
            if (d.type == 0) {
                color = Color.WHITE;
            } else {
                color = Color.RED;
            }
            holder.tv.setTextColor(color);
            holder.tv.setText(d.toString());
        }

        @Override
        public int getItemCount() {
            return len;
        }
    }

    private static final class VH extends RecyclerView.ViewHolder {
        TextView tv;
        public VH(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }

    private static final class Data {
        private StringBuilder sb;
        private byte type = 0;
        private int count;

        public String toString() {
            if (type == -1)
                return "⚠️same error as before, count: " + count;
            return getSB();
        }

        private String getSB() {
            if (sb != null)
                return sb.toString();
            return "";
        }

        public void append(String s) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append(s);
        }
    }
}