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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bvalosek.cpuspy.CpuSpyApp;
import com.bvalosek.cpuspy.CpuStateMonitor;
import com.hades.hKtweaks.R;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Log;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.kernel.cpu.CPUFreq;
import com.hades.hKtweaks.utils.kernel.gpu.GPUFreq;
import com.hades.hKtweaks.views.XYGraph;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.StatsView;
import com.hades.hKtweaks.views.recyclerview.overallstatistics.FrequencyButtonView;
import com.hades.hKtweaks.views.recyclerview.overallstatistics.FrequencyTableView;
import com.hades.hKtweaks.views.recyclerview.overallstatistics.TemperatureView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by willi on 19.04.16.
 */
public class OverallFragment extends RecyclerViewFragment {

    private CPUFreq mCPUFreq;
    private GPUFreq mGPUFreq;

    private StatsView mGPUFreqStatsView;
    private TemperatureView mTemperature;

    private CardView mFreqBig;
    private CardView mFreqMid;
    private CardView mFreqLITTLE;
    private CpuSpyApp mCpuSpyBig;
    private CpuSpyApp mCpuSpyMid;
    private CpuSpyApp mCpuSpyLITTLE;

    private double mBatteryRaw;

    private FrequencyTask mFrequencyTask;
    private Integer mGPUCurFreq;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBatteryRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10D;
        }
    };

    @Override
    protected void init() {
        super.init();

        mCPUFreq = CPUFreq.getInstance();
        mGPUFreq = GPUFreq.getInstance();
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        items.add(new CPUUsageFView());

        statsInit(items);
        frequenciesInit(items);
    }

    private void statsInit(List<RecyclerViewItem> items) {

        if (mGPUFreq.hasCurFreq()) {
            mGPUFreqStatsView = new StatsView();
            mGPUFreqStatsView.setTitle(getString(R.string.gpu_freq));

            items.add(mGPUFreqStatsView);
        }
        mTemperature = new TemperatureView();
        mTemperature.setFullSpan(mGPUFreqStatsView == null);

        items.add(mTemperature);
    }

    private void frequenciesInit(List<RecyclerViewItem> items) {
        FrequencyButtonView frequencyButtonView = new FrequencyButtonView();
        frequencyButtonView.setRefreshListener(v -> updateFrequency());
        frequencyButtonView.setResetListener(v -> {
            CpuStateMonitor cpuStateMonitor = mCpuSpyBig.getCpuStateMonitor();
            CpuStateMonitor cpuStateMonitorLITTLE = null;
            CpuStateMonitor cpuStateMonitorMid = null;
            if (mCpuSpyLITTLE != null) {
                cpuStateMonitorLITTLE = mCpuSpyLITTLE.getCpuStateMonitor();
                if (mCpuSpyMid != null) {
                    cpuStateMonitorMid = mCpuSpyMid.getCpuStateMonitor();
                }
            }
            try {
                cpuStateMonitor.setOffsets();
                if (cpuStateMonitorLITTLE != null) {
                    cpuStateMonitorLITTLE.setOffsets();
                    if (cpuStateMonitorMid != null) {
                        cpuStateMonitorMid.setOffsets();
                    }
                }
            } catch (CpuStateMonitor.CpuStateMonitorException ignored) {
            }
            mCpuSpyBig.saveOffsets();
            if (mCpuSpyLITTLE != null) {
                mCpuSpyLITTLE.saveOffsets();
                if (mCpuSpyMid != null) {
                    mCpuSpyMid.saveOffsets();
                }
            }
            updateView(cpuStateMonitor, mFreqBig);
            if (cpuStateMonitorLITTLE != null) {
                updateView(cpuStateMonitorLITTLE, mFreqLITTLE);
                if (cpuStateMonitorMid != null) {
                    updateView(cpuStateMonitorMid, mFreqMid);
                }
            }
        });
        frequencyButtonView.setRestoreListener(v -> {
            CpuStateMonitor cpuStateMonitor = mCpuSpyBig.getCpuStateMonitor();
            CpuStateMonitor cpuStateMonitorLITTLE = null;
            CpuStateMonitor cpuStateMonitorMid = null;
            if (mCpuSpyLITTLE != null) {
                cpuStateMonitorLITTLE = mCpuSpyLITTLE.getCpuStateMonitor();
                if (mCpuSpyMid != null) {
                    cpuStateMonitorMid = mCpuSpyMid.getCpuStateMonitor();
                }
            }
            cpuStateMonitor.removeOffsets();
            if (cpuStateMonitorLITTLE != null) {
                cpuStateMonitorLITTLE.removeOffsets();
                if (cpuStateMonitorMid != null) {
                    cpuStateMonitorMid.removeOffsets();
                }
            }
            mCpuSpyBig.saveOffsets();
            if (mCpuSpyLITTLE != null) {
                mCpuSpyLITTLE.saveOffsets();
                if (mCpuSpyMid != null) {
                    mCpuSpyMid.saveOffsets();
                }
            }
            updateView(cpuStateMonitor, mFreqBig);
            if (mCpuSpyLITTLE != null) {
                updateView(cpuStateMonitorLITTLE, mFreqLITTLE);
                if (mCpuSpyMid != null) {
                    updateView(cpuStateMonitorMid, mFreqMid);
                }
            }
        });
        items.add(frequencyButtonView);

        mFreqBig = new CardView(getActivity());
        if (mCPUFreq.isBigLITTLE()) {
            mFreqBig.setTitle(getString(R.string.cluster_big));
        } else {
            mFreqBig.setFullSpan(true);
        }
        items.add(mFreqBig);

        if (mCPUFreq.isBigLITTLE() && mCPUFreq.hasMidCpu()) {
            mFreqMid = new CardView(getActivity());
            mFreqMid.setTitle(getString(R.string.cluster_middle));
            items.add(mFreqMid);
        }

        if (mCPUFreq.isBigLITTLE()) {
            mFreqLITTLE = new CardView(getActivity());
            mFreqLITTLE.setTitle(getString(R.string.cluster_little));
            items.add(mFreqLITTLE);
        }

        mCpuSpyBig = new CpuSpyApp(mCPUFreq.getBigCpu(), getActivity());
        if (mCPUFreq.isBigLITTLE()) {
            mCpuSpyLITTLE = new CpuSpyApp(mCPUFreq.getLITTLECpu(), getActivity());
            if (mCPUFreq.hasMidCpu()) {
                mCpuSpyMid = new CpuSpyApp(mCPUFreq.getMidCpu(), getActivity());
            }
        }

        updateFrequency();
    }

    private void updateFrequency() {
        if (mFrequencyTask == null) {
            mFrequencyTask = new FrequencyTask();
            mFrequencyTask.execute(this);
        }
    }

    private void updateView(CpuStateMonitor monitor, CardView card) {
        if (!isAdded() || card == null) return;
        card.clearItems();

        // update the total state time
        DescriptionView totalTime = new DescriptionView();
        totalTime.setTitle(getString(R.string.uptime));
        totalTime.setSummary(sToString(monitor.getTotalStateTime() / 100L));
        card.addItem(totalTime);

        /* Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */
        List<String> extraStates = new ArrayList<>();
        for (CpuStateMonitor.CpuState state : monitor.getStates()) {
            if (state.getDuration() > 0) {
                generateStateRow(monitor, state, card);
            } else {
                if (state.getFreq() == 0) {
                    extraStates.add(getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.getFreq() / 1000 + getString(R.string.mhz));
                }
            }
        }

        if (monitor.getStates().size() == 0) {
            card.clearItems();
            DescriptionView errorView = new DescriptionView();
            errorView.setTitle(getString(R.string.error_frequencies));
            card.addItem(errorView);
            return;
        }

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            StringBuilder str = new StringBuilder();

            for (String s : extraStates) {
                if (n++ > 0)
                    str.append(", ");
                str.append(s);
            }

            DescriptionView unusedText = new DescriptionView();
            unusedText.setTitle(getString(R.string.unused_frequencies));
            unusedText.setSummary(str.toString());
            card.addItem(unusedText);
        }
    }

    /**
     * @return A nicely formatted String representing tSec seconds
     */
    private String sToString(long tSec) {
        int h = (int) tSec / 60 / 60;
        int m = (int) tSec / 60 % 60;
        int s = (int) tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10) sDur += "0";
        sDur += m + ":";
        if (s < 10) sDur += "0";
        sDur += s;

        return sDur;
    }

    /**
     * Creates a View that correpsonds to a CPU freq state row as specified
     * by the state parameter
     */
    private void generateStateRow(CpuStateMonitor monitor, CpuStateMonitor.CpuState state,
                                  CardView frequencyCard) {
        // what percentage we've got
        float per = (float) state.getDuration() * 100 / monitor.getTotalStateTime();

        String sFreq;
        if (state.getFreq() == 0) {
            sFreq = getString(R.string.deep_sleep);
        } else {
            sFreq = state.getFreq() / 1000 + getString(R.string.mhz);
        }

        // duration
        long tSec = state.getDuration() / 100;
        String sDur = sToString(tSec);

        FrequencyTableView frequencyState = new FrequencyTableView();
        frequencyState.setFrequency(sFreq);
        frequencyState.setPercentage((int) per);
        frequencyState.setDuration(sDur);

        frequencyCard.addItem(frequencyState);
    }

    @Override
    protected void refreshThread() {
        super.refreshThread();

        mGPUCurFreq = mGPUFreq.getCurFreq();
    }

    @Override
    protected void refresh() {
        super.refresh();

        if (mGPUFreqStatsView != null && mGPUCurFreq != null) {
            mGPUFreqStatsView.setStat(mGPUCurFreq / mGPUFreq.getCurFreqOffset() + getString(R.string.mhz));
        }
        if (mTemperature != null) {
            mTemperature.setBattery(mBatteryRaw);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(mBatteryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static class FrequencyTask extends AsyncTask<OverallFragment, Void, OverallFragment> {

        private CpuStateMonitor mBigMonitor;
        private CpuStateMonitor mMidMonitor;
        private CpuStateMonitor mLITTLEMonitor;

        @Override
        protected OverallFragment doInBackground(OverallFragment... overallFragments) {
            OverallFragment fragment = overallFragments[0];
            mBigMonitor = fragment.mCpuSpyBig.getCpuStateMonitor();
            if (fragment.mCPUFreq.isBigLITTLE()) {
                mLITTLEMonitor = fragment.mCpuSpyLITTLE.getCpuStateMonitor();
                if (fragment.mCPUFreq.hasMidCpu()) {
                    mMidMonitor = fragment.mCpuSpyMid.getCpuStateMonitor();
                }
            }
            try {
                mBigMonitor.updateStates();
            } catch (CpuStateMonitor.CpuStateMonitorException ignored) {
                Log.e("Problem getting CPU states");
            }
            if (fragment.mCPUFreq.isBigLITTLE()) {
                try {
                    mLITTLEMonitor.updateStates();
                    if (fragment.mCPUFreq.hasMidCpu()) {
                        mMidMonitor.updateStates();
                    }
                } catch (CpuStateMonitor.CpuStateMonitorException ignored) {
                    Log.e("Problem getting CPU states");
                }
            }
            return fragment;
        }

        @Override
        protected void onPostExecute(OverallFragment fragment) {
            super.onPostExecute(fragment);
            fragment.updateView(mBigMonitor, fragment.mFreqBig);
            if (fragment.mCPUFreq.isBigLITTLE()) {
                fragment.updateView(mLITTLEMonitor, fragment.mFreqLITTLE);
                if (fragment.mCPUFreq.hasMidCpu()) {
                    fragment.updateView(mMidMonitor, fragment.mFreqMid);
                }
            }
            fragment.mFrequencyTask = null;
        }
    }

    class CPUUsageFView extends RecyclerViewItem {
        private Handler mHandler;
        private List<View> mUsages;
        private Thread mThread;
        private float[] mCPUUsages;
        private int[] mFreqs;
        private Runnable mRefresh = new Runnable() {
            @Override
            public void run() {
                refresh();
                mHandler.postDelayed(this, 1000);
            }
        };

        @Override
        public void onCreateView(View view) {
            mHandler = new Handler();
            mUsages = new ArrayList<>();

            ((LinearLayout) view).removeAllViews();
            LinearLayout subView = null;
            for (int i = 0; i < CPUFreq.getInstance(getActivity()).getCpuCount(); i++) {
                if (subView == null || i % 2 == 0) {
                    subView = new LinearLayout(getContext());
                    subView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    ((LinearLayout) view).addView(subView);
                }

                View usage_view = getLayoutInflater().inflate(R.layout.fragment_usage_view, subView, false);
                usage_view.setLayoutParams(new LinearLayout
                        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                ((TextView) usage_view.findViewById(R.id.usage_core_text)).setText(getString(R.string.core, i + 1));
                mUsages.add(usage_view);
                subView.addView(usage_view);
            }

            onResume();
            setFullSpan(true);
            super.onCreateView(view);
        }

        @Override
        public int getLayoutRes() {
            return R.layout.fragment_linear_layout;
        }

        @Override
        public void onResume() {
            super.onResume();
            mHandler.post(mRefresh);
        }

        @Override
        public void onPause() {
            super.onPause();
            mHandler.removeCallbacks(mRefresh);
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
        }

        @Override
        public void refresh() {
            super.refresh();
            if (mThread == null) {
                mThread = new Thread(() -> {
                    while (true) {
                        if (mThread == null) break;
                        try {
                            CPUFreq cpuFreq = CPUFreq.getInstance(getActivity());
                            mCPUUsages = cpuFreq.getCpuUsage();
                            if (mFreqs == null) {
                                mFreqs = new int[cpuFreq.getCpuCount()];
                            }
                            for (int i = 0; i < mFreqs.length; i++) {
                                if (getActivity() == null) break;
                                mFreqs[i] = cpuFreq.getCurFreq(i);
                            }

                            if (getActivity() == null) {
                                mThread = null;
                            }
                        } catch (InterruptedException ignored) {
                            mThread = null;
                        }
                    }
                });
                mThread.start();
            }

            if (mFreqs == null || mCPUUsages == null || mUsages == null) return;
            for (int i = 0; i < mUsages.size(); i++) {
                View usageView = mUsages.get(i);
                TextView usageOfflineText = usageView.findViewById(R.id.usage_offline_text);
                TextView usageLoadText = usageView.findViewById(R.id.usage_load_text);
                TextView usageFreqText = usageView.findViewById(R.id.usage_freq_text);
                XYGraph usageGraph = usageView.findViewById(R.id.usage_graph);
                if (mFreqs[i] == 0) {
                    usageOfflineText.setVisibility(View.VISIBLE);
                    usageLoadText.setVisibility(View.GONE);
                    usageFreqText.setVisibility(View.GONE);
                    usageGraph.addPercentage(0);
                } else {
                    usageOfflineText.setVisibility(View.GONE);
                    usageLoadText.setVisibility(View.VISIBLE);
                    usageFreqText.setVisibility(View.VISIBLE);
                    usageFreqText.setText(Utils.strFormat("%d" + getString(R.string.mhz), mFreqs[i] / 1000));
                    usageLoadText.setText(Utils.strFormat("%d%%", Math.round(mCPUUsages[i + 1])));
                    usageGraph.addPercentage(Math.round(mCPUUsages[i + 1]));
                }
            }
        }

    }
}
