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
package com.hades.hKtweaks.fragments.tools.customcontrols;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.view.menu.MenuBuilder;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.FilePickerActivity;
import com.hades.hKtweaks.activities.tools.CustomControlsActivity;
import com.hades.hKtweaks.database.tools.customcontrols.Controls;
import com.hades.hKtweaks.database.tools.customcontrols.ExportControl;
import com.hades.hKtweaks.database.tools.customcontrols.ImportControl;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.tools.customcontrols.CustomControlException;
import com.hades.hKtweaks.utils.tools.customcontrols.Items;
import com.hades.hKtweaks.utils.tools.customcontrols.Values;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionFView;
import com.hades.hKtweaks.views.recyclerview.GenericSelectView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SeekBarView;
import com.hades.hKtweaks.views.recyclerview.SwitchView;
import com.hades.hKtweaks.views.recyclerview.customcontrols.ErrorView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by willi on 30.06.16.
 */
public class CustomControlsFragment extends RecyclerViewFragment {

    private Dialog mOptionsDialog;
    private Dialog mItemsDialog;
    private Dialog mDeleteDialog;

    private AsyncTask<Void, Void, ImportControl> mImportingThread;
    private Controls mControlsProvider;
    private Controls.ControlItem mExportItem;

    private void showControls() {
        mItemsDialog = new Dialog(getActivity()).setItems(getResources().getStringArray(
                R.array.custom_controls_items), (dialog, which) -> {
            Intent i = new Intent(getActivity(), CustomControlsActivity.class);
            i.putParcelableArrayListExtra(CustomControlsActivity.SETTINGS_INTENT, Items.getSettings(which));
            startActivityForResult(i, 0);
        }).setOnDismissListener(dialog -> mItemsDialog = null);
        mItemsDialog.show();
    }

    @Override
    protected void init() {
        super.init();
        showToolbarActionButton(item -> {
            mOptionsDialog = new Dialog(getActivity()).setItems(getResources().getStringArray(
                    R.array.custom_controls_options),
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showControls();
                                break;
                            case 1:
                                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                                intent.putExtra(FilePickerActivity.PATH_INTENT,
                                        Environment.getExternalStorageDirectory().toString());
                                intent.putExtra(FilePickerActivity.EXTENSION_INTENT, ".json");
                                startActivityForResult(intent, 1);

                                break;
                        }
                    })
                    .setOnDismissListener(dialog -> mOptionsDialog = null);
            if (!getActivity().isFinishing()) {
                try {
                    mOptionsDialog.show();
                } catch (NullPointerException ignored) {
                }
            }
        }, R.id.menu_add);

        try {
            if (mOptionsDialog != null) {
                mOptionsDialog.show();
            }
            if (mItemsDialog != null) {
                mItemsDialog.show();
            }
            if (mDeleteDialog != null) {
                mDeleteDialog.show();
            }
            if (mExportItem != null) {
                showExportDialog();
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        load(items);
    }

    private void reload() {
        clearItems();
        reload(new ReloadHandler<>());
    }

    @Override
    protected void load(List<RecyclerViewItem> items) {
        super.load(items);

        items.add(new DescriptionFView(getActivity(), getString(R.string.welcome), Utils.htmlFrom(getString(R.string.custom_controls_summary) + " " + getString(R.string.custom_controls_example_summary))));

        mControlsProvider = new Controls(getActivity());
        for (final Controls.ControlItem item : mControlsProvider.getAllControls()) {
            CardView cardView = getCard(item);
            cardView.clearItems();

            try {
                String title = item.getTitle();
                String description = item.getDescription();
                if (item.getControl() == Items.Control.SWITCH) {
                    SwitchView switchView = new SwitchView();
                    if (description != null) {
                        switchView.setTitle(title);
                        switchView.setSummary(description);
                    } else {
                        switchView.setSummary(title);
                    }

                    switchView.setChecked(Values.getBool(item.getString("enable")));
                    switchView.addOnSwitchListener((switchView1, isChecked) -> {
                        Values.run(item.getApply(), item, isChecked ? "1" : "0");
                        mControlsProvider.commit();
                    });

                    cardView.addItem(switchView);
                } else if (item.getControl() == Items.Control.SEEKBAR) {
                    SeekBarView seekBarView = new SeekBarView();
                    seekBarView.setTitle(title);
                    if (description != null) {
                        seekBarView.setSummary(description);
                    }

                    seekBarView.setMax(item.getInt("max"));
                    seekBarView.setMin(item.getInt("min"));
                    seekBarView.setProgress(Values.getInt(item.getString("progress")));
                    seekBarView.setOnSeekBarListener(new SeekBarView.OnSeekBarListener() {
                        @Override
                        public void onStop(SeekBarView seekBarView, int position, String value) {
                            Values.run(item.getApply(), item, String.valueOf(position));
                            mControlsProvider.commit();
                        }

                        @Override
                        public void onMove(SeekBarView seekBarView, int position, String value) {
                        }
                    });

                    cardView.addItem(seekBarView);
                } else if (item.getControl() == Items.Control.GENERIC) {
                    GenericSelectView genericSelectView = new GenericSelectView();
                    if (description != null) {
                        genericSelectView.setTitle(title);
                        genericSelectView.setSummary(description);
                    } else {
                        genericSelectView.setSummary(title);
                    }

                    genericSelectView.setValue(Values.getString(item.getString("value")));
                    genericSelectView.setValueRaw(genericSelectView.getValue());
                    genericSelectView.setOnGenericValueListener((genericSelectView1, value) -> {
                        Values.run(item.getApply(), item, value);
                        genericSelectView1.setValue(value);
                        mControlsProvider.commit();
                    });

                    cardView.addItem(genericSelectView);
                }
            } catch (CustomControlException e) {
                ErrorView errorView = new ErrorView();
                errorView.setException(e);
                cardView.addItem(errorView);
            }

            items.add(cardView);
        }
    }

    private CardView getCard(final Controls.ControlItem controlItem) {
        CardView cardView = new CardView(getActivity());
        cardView.setOnMenuListener((cardView1, popupMenu) -> {

            @SuppressLint("RestrictedApi") Menu menu = new MenuBuilder(getContext());
            menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.edit));
            final MenuItem onBoot = menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.on_boot) + ": " + controlItem.isOnBootEnabled());
            onBoot.setCheckable(true).setChecked(controlItem.isOnBootEnabled());
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.export));
            menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.delete));

            ArrayList<MenuItem> menuItems = new ArrayList<>();
            for (int i = 0; i < menu.size(); i++) menuItems.add(menu.getItem(i));
            popupMenu.inflate(menuItems);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        edit(controlItem);
                        break;
                    case 1:
                        onBoot.setChecked(!onBoot.isChecked());
                        controlItem.enableOnBoot(onBoot.isChecked());

                        //todo: new popupmenu
                        onBoot.setTitle(getString(R.string.on_boot) + ": " + onBoot.isChecked());
                        Toast.makeText(getContext(), getString(R.string.on_boot) + ": " + onBoot.isChecked(), Toast.LENGTH_SHORT).show();

                        mControlsProvider.commit();
                        break;
                    case 2:
                        mExportItem = controlItem;
                        requestPermission(0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        break;
                    case 3:
                        mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                                (dialog, which) -> {
                                },
                                (dialog, which) -> delete(controlItem.getUniqueId()),
                                dialog -> mDeleteDialog = null, getActivity())
                                .setTitle(getString(R.string.delete));
                        mDeleteDialog.show();
                        break;
                }
                popupMenu.dismiss();
            });
        });
        return cardView;
    }

    private void delete(int uniqueId) {
        List<Controls.ControlItem> items = mControlsProvider.getAllControls();
        for (int i = 0; i < items.size(); i++) {
            if (uniqueId == items.get(i).getUniqueId()) {
                mControlsProvider.delete(i);
            }
        }
        mControlsProvider.commit();
        reload();
    }

    private void edit(Controls.ControlItem controlItem) {
        ArrayList<Items.Setting> settings = new ArrayList<>();
        settings.add(new Items.Setting("id", controlItem.getUniqueId(), 0, 0, controlItem.getId(), null,
                null, false, false, Items.Setting.Unit.ID));
        settings.add(new Items.Setting("title", R.string.title, controlItem.getTitle(), true, Items.Setting.Unit.STRING));
        settings.add(new Items.Setting("description", R.string.description, controlItem.getDescription(),
                false, Items.Setting.Unit.STRING));

        if (controlItem.getControl() == Items.Control.SWITCH) {
            settings.add(new Items.Setting("enable", R.string.enabled, R.string.switch_enabled_summary,
                    controlItem.getString("enable"), true, true, Items.Setting.Unit.BOOLEAN));
            settings.add(new Items.Setting("apply", R.string.applying, R.string.switch_apply_summary,
                    controlItem.getApply(), true, true, Items.Setting.Unit.APPLY));
        } else if (controlItem.getControl() == Items.Control.SEEKBAR) {
            settings.add(new Items.Setting("min", R.string.seekbar_min, String.valueOf(controlItem.getInt("min")),
                    true, Items.Setting.Unit.INTEGER));
            settings.add(new Items.Setting("max", R.string.seekbar_max, String.valueOf(controlItem.getInt("max")),
                    true, Items.Setting.Unit.INTEGER));
            settings.add(new Items.Setting("progress", R.string.seekbar_progress, R.string.seekbar_progress_summary,
                    controlItem.getString("progress"), true, true, Items.Setting.Unit.INTEGER));
            settings.add(new Items.Setting("apply", R.string.applying, R.string.seekbar_apply_summary,
                    controlItem.getApply(), true, true, Items.Setting.Unit.APPLY));
        } else if (controlItem.getControl() == Items.Control.GENERIC) {
            settings.add(new Items.Setting("value", R.string.generic_value, R.string.generic_value_summary,
                    controlItem.getString("value"), true, true, Items.Setting.Unit.STRING));
            settings.add(new Items.Setting("apply", R.string.applying, R.string.generic_apply_summary,
                    controlItem.getApply(), true, true, Items.Setting.Unit.APPLY));
        }

        Intent i = new Intent(getActivity(), CustomControlsActivity.class);
        i.putParcelableArrayListExtra(CustomControlsActivity.SETTINGS_INTENT, settings);
        startActivityForResult(i, 0);
    }

    private void showExportDialog() {
        ViewUtils.dialogEditText(null,
                (dialog, which) -> {
                },
                text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }
                    if (new ExportControl(mExportItem, mControlsProvider.getVersion()).export(text)) {
                        Utils.toast(getString(R.string.exported_item, text, Utils.getInternalDataStorage()
                                + "/controls"), getActivity());
                    } else {
                        Utils.toast(getString(R.string.already_exists, text), getActivity());
                    }
                }, getActivity()).setTitle(getString(R.string.name))
                .setOnDismissListener(dialog -> mExportItem = null).show();
    }

    private void importing(final String path) {
        if (mImportingThread == null) {
            mImportingThread = new ImportTask(this, path);
            mImportingThread.execute();
        }
    }

    @Override
    public void onPermissionGranted(int request) {
        super.onPermissionGranted(request);
        if (request == 0) {
            showExportDialog();
        }
    }

    @Override
    public void onPermissionDenied(int request) {
        super.onPermissionDenied(request);
        if (request == 0) {
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
            mExportItem = null;
        }
    }

    private void updateControls(HashMap<String, Object> result) {
        List<Controls.ControlItem> items = mControlsProvider.getAllControls();
        int uniqueId;
        try {
            uniqueId = (int) result.get("uniqueId");
        } catch (ClassCastException ignored) {
            uniqueId = 0;
        }
        for (int i = 0; i < items.size(); i++) {
            if (uniqueId != 0 && uniqueId == items.get(i).getUniqueId()) {
                mControlsProvider.delete(i);
            }
        }
        if (uniqueId == 0) {
            result.put("uniqueId", Values.getUniqueId(items));
        }
        mControlsProvider.putItem(result);
        mControlsProvider.commit();
        reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == 0) {
                HashMap<String, Object> results = (HashMap<String, Object>)
                        data.getSerializableExtra(CustomControlsActivity.RESULT_INTENT);
                updateControls(results);
            } else if (requestCode == 1) {
                importing(data.getStringExtra(FilePickerActivity.RESULT_INTENT));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImportingThread != null) {
            mImportingThread.cancel(true);
            mImportingThread = null;
        }
    }

    private static class ImportTask extends AsyncTask<Void, Void, ImportControl> {

        private WeakReference<CustomControlsFragment> mRefFragment;
        private String mPath;

        private ImportTask(CustomControlsFragment fragment, String path) {
            mRefFragment = new WeakReference<>(fragment);
            mPath = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CustomControlsFragment fragment = mRefFragment.get();
            if (fragment != null) {
                fragment.showProgress();
            }
        }

        @Override
        protected ImportControl doInBackground(Void... voids) {
            CustomControlsFragment fragment = mRefFragment.get();
            if (fragment != null) {
                return new ImportControl(mPath);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ImportControl importControl) {
            super.onPostExecute(importControl);
            CustomControlsFragment fragment = mRefFragment.get();
            if (fragment == null) return;

            fragment.hideProgress();
            fragment.mImportingThread = null;
            if (!importControl.readable()) {
                Utils.toast(R.string.import_malformed, fragment.getActivity());
            } else if (!importControl.matchesVersion()) {
                Utils.toast(R.string.import_wrong_version, fragment.getActivity());
            } else {
                fragment.updateControls(importControl.getResults());
            }
        }
    }
}
