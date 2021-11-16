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
package com.hades.hKtweaks.fragments.kernel;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.tools.profile.ProfileActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.kernel.entropy.Entropy;
import com.hades.hKtweaks.views.recyclerview.ApplyOnBootFView;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.ProgressBarView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SeekBarView;

import java.util.List;

/**
 * Created by willi on 29.06.16.
 */
public class EntropyFragment extends RecyclerViewFragment {

    private ProgressBarView mProgressBarView;
    private Integer mPoolSize;
    private Integer mAvailable;

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        if (!(getActivity() instanceof ProfileActivity))
            items.add(new ApplyOnBootFView(getActivity(), this));

        CardView cardView = new CardView(getActivity());
        cardView.setExpandable(false);
        mProgressBarView = new ProgressBarView();
        mProgressBarView.setTitle(getString(R.string.poolsize));
        mProgressBarView.setItems(Entropy.getPoolsize(), Entropy.getAvailable());
        mProgressBarView.setProgressColor(getResources().getColor(R.color.green));
        mProgressBarView.setFullSpan(true);
        cardView.addItem(mProgressBarView);
        items.add(cardView);

        SeekBarView read = new SeekBarView();
        read.setFullSpan(true);
        read.setTitle(getString(R.string.read));
        read.setMax(4096);
        read.setMin(64);
        read.setOffset(64);
        read.setProgress(Entropy.getRead() / 64 - 1);
        read.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
            @Override
            public void onStop(SeekBarView seekBarView, int position, String value) {
                Entropy.setRead((position + 1) * 64, getActivity());
            }

            @Override
            public void onMove(SeekBarView seekBarView, int position, String value) {
            }
        });

        items.add(read);

        SeekBarView write = new SeekBarView();
        write.setFullSpan(true);
        write.setTitle(getString(R.string.write));
        write.setMax(4096);
        write.setMin(64);
        write.setOffset(64);
        write.setProgress(Entropy.getWrite() / 64 - 1);
        write.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
            @Override
            public void onStop(SeekBarView seekBarView, int position, String value) {
                Entropy.setWrite((position + 1) * 64, getActivity());
            }

            @Override
            public void onMove(SeekBarView seekBarView, int position, String value) {
            }
        });

        items.add(write);
    }

    @Override
    protected void refreshThread() {
        super.refreshThread();

        mPoolSize = Entropy.getPoolsize();
        mAvailable = Entropy.getAvailable();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mPoolSize != null && mAvailable != null && mProgressBarView != null) {
            mProgressBarView.setItems(mPoolSize, mAvailable);
        }
    }
}
