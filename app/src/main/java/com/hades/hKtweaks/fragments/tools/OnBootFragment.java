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
package com.hades.hKtweaks.fragments.tools;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.database.Settings;
import com.hades.hKtweaks.database.tools.customcontrols.Controls;
import com.hades.hKtweaks.database.tools.profiles.Profiles;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionFView;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by willi on 04.08.16.
 */
public class OnBootFragment extends RecyclerViewFragment {

    private Settings mSettings;
    private Controls mControls;
    private Profiles mProfiles;

    private Dialog mDeleteDialog;

    @Override
    protected void init() {
        super.init();

        if (mDeleteDialog != null) {
            mDeleteDialog.show();
        }

        if (mSettings == null) {
            mSettings = new Settings(getActivity());
        }
        if (mControls == null) {
            mControls = new Controls(getActivity());
        }
        if (mProfiles == null) {
            mProfiles = new Profiles(getActivity());
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        load(items);
    }

    private void reload() {
        getHandler().postDelayed(() -> {
            clearItems();
            reload(new ReloadHandler<>());
        }, 250);
    }

    @Override
    protected void load(List<RecyclerViewItem> items) {
        super.load(items);
        items.add(new DescriptionFView(getActivity(), getString(R.string.welcome), getString(R.string.on_boot_welcome_summary)));


        CardView applyOnBootTitle = new CardView(getActivity());
        applyOnBootTitle.setTitle(getString(R.string.apply_on_boot));

        List<Settings.SettingsItem> settings = mSettings.getAllSettings();
        HashMap<String, Boolean> applyOnBootEnabled = new HashMap<>();
        List<ApplyOnBootItem> applyOnBootItems = new ArrayList<>();
        for (int i = 0; i < settings.size(); i++) {
            Settings.SettingsItem item = settings.get(i);
            boolean enabled;
            if (applyOnBootEnabled.containsKey(item.getCategory())) {
                enabled = applyOnBootEnabled.get(item.getCategory());
            } else {
                applyOnBootEnabled.put(item.getCategory(),
                        enabled = AppSettings.getBoolean(settings.get(i).getCategory(),
                                false, getActivity()));
            }
            if (enabled) {
                applyOnBootItems.add(new ApplyOnBootItem(item.getSetting(),
                        item.getCategory(), i));
            }
        }

        for (int i = 0; i < applyOnBootItems.size(); i++) {
            final ApplyOnBootItem applyOnBootItem = applyOnBootItems.get(i);
            DescriptionView applyOnBootView = new DescriptionView();
            applyOnBootView.setSummary(
                    (i + 1)
                            + ". " + applyOnBootItem.mCategory.replace("_onboot", "")
                            + ": " + applyOnBootItem.mCommand);

            applyOnBootView.setOnItemClickListener(item -> {
                mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.delete_question,
                        applyOnBootItem.mCommand),
                        (dialogInterface, i1) -> {
                        },
                        (dialogInterface, i1) -> {
                            mSettings.delete(applyOnBootItem.mPosition);
                            mSettings.commit();
                            reload();
                        },
                        dialogInterface -> mDeleteDialog = null, getActivity());
                mDeleteDialog.show();
            });

            applyOnBootTitle.addItem(applyOnBootView);
        }
        if (applyOnBootTitle.size() > 0) items.add(applyOnBootTitle);


        CardView customControlTitle = new CardView(getActivity());
        customControlTitle.setTitle(getString(R.string.custom_controls));

        for (final Controls.ControlItem controlItem : mControls.getAllControls()) {
            if (controlItem.isOnBootEnabled() && controlItem.getArguments() != null) {
                DescriptionView controlView = new DescriptionView();
                controlView.setTitle(controlItem.getTitle());
                controlView.setSummary(getString(R.string.arguments, controlItem.getArguments()));
                controlView.setOnItemClickListener(item -> {
                    mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.disable_question,
                            controlItem.getTitle()),
                            (dialogInterface, i) -> {
                            },
                            (dialogInterface, i) -> {
                                controlItem.enableOnBoot(false);
                                mControls.commit();
                                reload();
                            },
                            dialogInterface -> mDeleteDialog = null, getActivity());
                    mDeleteDialog.show();
                });

                customControlTitle.addItem(controlView);
            }
        }
        if (customControlTitle.size() > 0) items.add(customControlTitle);


        CardView profileTitle = new CardView(getActivity());
        profileTitle.setTitle(getString(R.string.profile));

        for (final Profiles.ProfileItem profileItem : mProfiles.getAllProfiles()) {
            if (profileItem.isOnBootEnabled()) {
                DescriptionView profileView = new DescriptionView();
                profileView.setSummary(profileItem.getName());
                profileView.setOnItemClickListener(item -> {
                    mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.disable_question,
                            profileItem.getName()),
                            (dialogInterface, i) -> {
                            },
                            (dialogInterface, i) -> {
                                profileItem.enableOnBoot(false);
                                mProfiles.commit();
                                reload();
                            },
                            dialogInterface -> mDeleteDialog = null, getActivity());
                    mDeleteDialog.show();
                });

                profileTitle.addItem(profileView);
            }
        }
        if (profileTitle.size() > 0) items.add(profileTitle);


        if (AppSettings.isInitdOnBoot(getActivity())) {
            CardView initdTitle = new CardView(getActivity());
            initdTitle.setTitle(getString(R.string.initd));

            DescriptionView emulateInitd = new DescriptionView();
            emulateInitd.setSummary(getString(R.string.emulate_initd));
            emulateInitd.setOnItemClickListener(item -> {
                mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.disable_question,
                        getString(R.string.emulate_initd)),
                        (dialogInterface, i) -> {
                        },
                        (dialogInterface, i) -> {
                            AppSettings.saveInitdOnBoot(false, getActivity());
                            reload();
                        },
                        dialogInterface -> mDeleteDialog = null, getActivity());
                mDeleteDialog.show();
            });

            initdTitle.addItem(emulateInitd);
            items.add(initdTitle);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSettings = null;
        mControls = null;
        mProfiles = null;
    }

    private class ApplyOnBootItem {
        private final String mCommand;
        private final String mCategory;
        private final int mPosition;

        private ApplyOnBootItem(String command, String category, int position) {
            mCommand = command;
            mCategory = category;
            mPosition = position;
        }
    }
}
