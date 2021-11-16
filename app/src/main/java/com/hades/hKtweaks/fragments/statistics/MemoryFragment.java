/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.hades.hKtweaks.fragments.statistics;

import android.content.res.Configuration;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Device;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.StatsView;

import java.util.List;

/**
 * Created by willi on 05.08.16.
 */
public class MemoryFragment extends RecyclerViewFragment {

    private Device.MemInfo mMemInfo;

    @Override
    protected void init() {
        super.init();
        getRecyclerView().setBackgroundResource(R.color.item_background_color);
        mMemInfo = Device.MemInfo.getInstance();
    }

    @Override
    public int getSpanCount() {
        int span = Utils.isTablet(getActivity()) ? Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 6 : 5 : Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 4 : 3;
        if (itemsSize() != 0 && span > itemsSize()) {
            span = itemsSize();
        }
        return span;
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        List<String> mems = mMemInfo.getItems();
        for (String mem : mems) {
            StatsView memView = new StatsView();
            memView.setTitle(mem);
            memView.setStat(mMemInfo.getItem(mem).replace(" ", "").replace("kB", getString(R.string.kb)));

            items.add(memView);
        }
    }
}
