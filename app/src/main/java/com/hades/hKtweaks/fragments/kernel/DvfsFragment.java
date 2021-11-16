package com.hades.hKtweaks.fragments.kernel;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.tools.profile.ProfileActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.kernel.dvfs.Dvfs;
import com.hades.hKtweaks.views.recyclerview.ApplyOnBootFView;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SeekBarView;
import com.hades.hKtweaks.views.recyclerview.SelectView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Morogoku on 11/04/2017.
 */

public class DvfsFragment extends RecyclerViewFragment {

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        if (!(getActivity() instanceof ProfileActivity))
            items.add(new ApplyOnBootFView(getActivity(), this));

        CardView dec = new CardView(getActivity());
        dec.setTitle(getString(R.string.dvfs_decision_mode));

        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("Battery", "Balance", "Performance"));

        SelectView selectView = new SelectView();
        selectView.setTitle(getString(R.string.dvfs_decision_mode));
        selectView.setSummary(getString(R.string.dvfs_decision_mode_summary));
        selectView.setItems(list);
        selectView.setItem(Dvfs.getDecisionMode());
        selectView.setOnItemSelected(new SelectView.OnItemSelected() {
            @Override
            public void onItemSelected(SelectView selectView, int position, String item) {
                Dvfs.setDecisionMode(item, getActivity());
            }
        });

        dec.addItem(selectView);

        if (dec.size() > 0) {
            items.add(dec);
        }


        CardView term = new CardView(getActivity());
        term.setTitle(getString(R.string.dvfs_thermal_control));

        SeekBarView seekBar = new SeekBarView();
        seekBar.setTitle(getString(R.string.dvfs_thermal_control));
        seekBar.setSummary(getString(R.string.dvfs_thermal_control_summary));
        seekBar.setMax(90);
        seekBar.setMin(40);
        seekBar.setProgress(Utils.strToInt(Dvfs.getThermalControl()) - 40);
        seekBar.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
            @Override
            public void onStop(SeekBarView seekBarView, int position, String value) {
                Dvfs.setThermalControl(String.valueOf(position + 40), getActivity());
            }

            @Override
            public void onMove(SeekBarView seekBarView, int position, String value) {
            }
        });

        term.addItem(seekBar);

        if (term.size() > 0) {
            items.add(term);
        }
    }

}
