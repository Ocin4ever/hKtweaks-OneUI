package com.hades.hKtweaks.fragments.kernel;

import android.view.View;
import android.widget.CheckBox;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.tools.profile.ProfileActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.kernel.boefflawakelock.BoefflaWakelock;
import com.hades.hKtweaks.utils.kernel.boefflawakelock.WakeLockInfo;
import com.hades.hKtweaks.views.recyclerview.ApplyOnBootFView;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SelectView;
import com.hades.hKtweaks.views.recyclerview.SwitchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;

/**
 * Created by MoroGoku on 10/11/2017.
 */

public class BoefflaWakelockFragment extends RecyclerViewFragment {

    private final List<CardView> mWakeCard = new ArrayList<>();
    boolean mAlertCheckbox = true;

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        if (!(getActivity() instanceof ProfileActivity))
            items.add(new ApplyOnBootFView(getActivity(), this));

        if (BoefflaWakelock.supported()) {
            boefflaWakelockInit(items);
        }
    }

    private void boefflaWakelockInit(List<RecyclerViewItem> items) {
        mWakeCard.clear();

        CardView bwbT = new CardView(getActivity());
        bwbT.setTitle(getString(R.string.boeffla_wakelock) + " v" + BoefflaWakelock.getVersion());

        DescriptionView bwbD = new DescriptionView();
        bwbD.setSummary(getString(R.string.boeffla_wakelock_summary));
        bwbT.addItem(bwbD);

        SelectView bwOrder = new SelectView();
        bwOrder.setTitle(getString(R.string.wkl_order));
        bwOrder.setSummary(getString(R.string.wkl_order_summary));
        bwOrder.setItems(Arrays.asList(getResources().getStringArray(R.array.b_wakelocks_oder)));
        bwOrder.setItem(BoefflaWakelock.getWakelockOrder());
        bwOrder.setOnItemSelected((selectView, position, item) -> {
            BoefflaWakelock.setWakelockOrder(position);
            bwCardReload();
        });
        bwOrder.setFullSpan(true);
        bwbT.addItem(bwOrder);

        items.add(bwbT);


        List<WakeLockInfo> wakelocksinfo = BoefflaWakelock.getWakelockInfo();

        CardView cardViewB = new CardView(getActivity());
        String titleB = getString(R.string.wkl_blocked);
        grxbwCardInit(cardViewB, titleB, wakelocksinfo, false);
        mWakeCard.add(cardViewB);

        CardView cardViewA = new CardView(getActivity());
        String titleA = getString(R.string.wkl_allowed);
        grxbwCardInit(cardViewA, titleA, wakelocksinfo, true);
        mWakeCard.add(cardViewA);

        items.addAll(mWakeCard);
    }


    private void grxbwCardInit(CardView card, String title, List<WakeLockInfo> wakelocksinfo, Boolean state) {
        card.clearItems();
        card.setTitle(title);

        for (WakeLockInfo wakeLockInfo : wakelocksinfo) {

            if (wakeLockInfo.wState == state) {

                final String name = wakeLockInfo.wName;
                String wakeup = String.valueOf(wakeLockInfo.wWakeups);
                String time = String.valueOf(wakeLockInfo.wTime / 1000);
                time = Utils.sToString(Utils.strToLong(time));

                SwitchView sw = new SwitchView();
                sw.setTitle(name);
                sw.setSummary(getString(R.string.wkl_total_time) + ": " + time + "\n" +
                        getString(R.string.wkl_wakep_count) + ": " + wakeup);
                sw.setChecked(wakeLockInfo.wState);
                sw.addOnSwitchListener((switchView, isChecked) -> {
                    if (isChecked) {
                        BoefflaWakelock.setWakelockAllowed(name, getActivity());
                    } else {
                        BoefflaWakelock.setWakelockBlocked(name, getActivity());
                    }
                    getHandler().postDelayed(this::bwCardReload, 250);
                });

                card.addItem(sw);
            }
        }
    }

    private void bwCardReload() {

        List<WakeLockInfo> wakelocksinfo = BoefflaWakelock.getWakelockInfo();

        String titleB = getString(R.string.wkl_blocked);
        grxbwCardInit(mWakeCard.get(0), titleB, wakelocksinfo, false);

        String titleA = getString(R.string.wkl_allowed);
        grxbwCardInit(mWakeCard.get(1), titleA, wakelocksinfo, true);

    }

    private void warningDialog() {

        View checkBoxView = View.inflate(getActivity(), R.layout.alertdialog_wakelock_fragment, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.chbox);
        checkBox.setChecked(true);
        checkBox.setText(getString(R.string.wkl_alert_checkbox));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                -> mAlertCheckbox = isChecked);

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.wkl_alert_title)
                .setMessage(R.string.wkl_alert_message)
                .setView(checkBoxView)
                .setPositiveButton(R.string.ok, (dialog, id)
                        -> AppSettings.saveBoolean("show_wakelock_dialog", mAlertCheckbox, getActivity()))
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean showDialog = AppSettings.getBoolean("show_wakelock_dialog", true, getActivity());

        if (showDialog) warningDialog();

    }
}
