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

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.FilePickerActivity;
import com.hades.hKtweaks.activities.NavigationActivity;
import com.hades.hKtweaks.activities.tools.profile.ProfileActivity;
import com.hades.hKtweaks.activities.tools.profile.ProfileEditActivity;
import com.hades.hKtweaks.activities.tools.profile.ProfileTaskerActivity;
import com.hades.hKtweaks.database.tools.profiles.ExportProfile;
import com.hades.hKtweaks.database.tools.profiles.ImportProfile;
import com.hades.hKtweaks.database.tools.profiles.Profiles;
import com.hades.hKtweaks.fragments.BaseFragment;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.services.boot.ApplyOnBoot;
import com.hades.hKtweaks.services.profile.Tile;
import com.hades.hKtweaks.services.profile.Widget;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.kernel.cpu.CPUFreq;
import com.hades.hKtweaks.utils.root.Control;
import com.hades.hKtweaks.utils.root.RootUtils;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionFView;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SwitcherFView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.menu.Menu;
import de.dlyt.yanndroid.oneui.menu.MenuItem;
import de.dlyt.yanndroid.oneui.menu.PopupMenu;

/**
 * Created by willi on 10.07.16.
 */
public class ProfileFragment extends RecyclerViewFragment {

    private static final String TASKER_KEY = "tasker";
    private boolean mTaskerMode;
    private Profiles mProfiles;
    private LinkedHashMap<String, String> mCommands;
    private Dialog mDeleteDialog;
    private Dialog mApplyDialog;
    private Profiles.ProfileItem mExportProfile;
    private Dialog mOptionsDialog;
    private Dialog mDonateDialog;
    private ImportProfile mImportProfile;
    private Dialog mSelectDialog;
    private DetailsFragment mDetailsFragment;

    public static ProfileFragment newInstance(boolean tasker) {
        Bundle args = new Bundle();
        args.putBoolean(TASKER_KEY, tasker);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTaskerMode = getArguments().getBoolean(TASKER_KEY);
        }
    }

    @Override
    protected BaseFragment getForegroundFragment() {
        return mTaskerMode ? null : (mDetailsFragment = new DetailsFragment());
    }

    @Override
    public int getSpanCount() {
        int span = Utils.isTablet(getActivity()) ? Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 4 : 3 : Utils.getOrientation(getActivity()) ==
                Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        if (itemsSize() != 0 && span > itemsSize()) {
            span = itemsSize();
        }
        return span;
    }

    @Override
    protected void init() {
        super.init();
        if (!mTaskerMode) showToolbarActionButton(item -> {
            mOptionsDialog = new Dialog(getActivity()).setItems(
                    getResources().getStringArray(R.array.profile_options),
                    (dialogInterface, i) -> {
                        switch (i) {
                            case 0:
                                startActivityForResult(createProfileActivityIntent(), 0);
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
                    .setOnDismissListener(dialogInterface -> mOptionsDialog = null);
            mOptionsDialog.show();
            return true;
        }, R.id.menu_add);

        if (mCommands != null) {
            create(mCommands);
        }
        if (mDeleteDialog != null) {
            mDeleteDialog.show();
        }
        if (mApplyDialog != null) {
            mApplyDialog.show();
        }
        if (mExportProfile != null) {
            showExportDialog();
        }
        if (mOptionsDialog != null) {
            mOptionsDialog.show();
        }
        if (mDonateDialog != null) {
            mDonateDialog.show();
        }
        if (mImportProfile != null) {
            showImportDialog(mImportProfile);
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

        if (mTaskerMode) {
            items.add(new SwitcherFView("", getString(R.string.profile_tasker_toast), AppSettings.isShowTaskerToast(getActivity()), (buttonView, isChecked) -> AppSettings.saveShowTaskerToast(isChecked, getActivity())));
        } else {
            items.add(new DescriptionFView(getActivity(), getString(R.string.profile_tasker),
                    getString(R.string.profile_tasker_summary)));

            if (Utils.hasCMSDK()) {
                items.add(new SwitcherFView(getString(R.string.profile_tile),
                        getString(R.string.profile_tile_summary),
                        AppSettings.isProfileTile(getActivity()),
                        (compoundButton, b) -> {
                            AppSettings.saveProfileTile(b, getActivity());
                            Tile.publishProfileTile(mProfiles.getAllProfiles(), getActivity());
                        }));
            }
        }

        mProfiles = new Profiles(getActivity());
        List<Profiles.ProfileItem> profileItems = mProfiles.getAllProfiles();
        if (mTaskerMode && profileItems.size() == 0) {
            Snackbar.make(getRootView(), R.string.no_profiles, Snackbar.LENGTH_LONG).show();
            return;
        }
        for (int i = 0; i < profileItems.size(); i++) {
            final int position = i;
            final CardView cardView = new CardView(getActivity());
            cardView.setOnMenuListener((cardView1, popupMenu) -> {


                Menu menu = new Menu();
                menu.addMenuItem(new MenuItem(0, getString(R.string.append), null));
                menu.addMenuItem(new MenuItem(1, getString(R.string.edit), null));
                menu.addMenuItem(new MenuItem(2, getString(R.string.details), null));

                MenuItem onBoot = new MenuItem(3, getString(R.string.on_boot), null);
                onBoot.setCheckable(true);
                onBoot.setChecked(mProfiles.getAllProfiles().get(position).isOnBootEnabled());
                menu.addMenuItem(onBoot);

                menu.addMenuItem(new MenuItem(4, getString(R.string.export), null));
                menu.addMenuItem(new MenuItem(5, getString(R.string.delete), null));

                popupMenu.inflate(menu);
                popupMenu.setPopupMenuListener(new PopupMenu.PopupMenuListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        List<Profiles.ProfileItem> items1 = mProfiles.getAllProfiles();
                        switch (menuItem.getItemId()) {
                            case 0:
                                Intent intent = createProfileActivityIntent();
                                intent.putExtra(ProfileActivity.POSITION_INTENT, position);
                                startActivityForResult(intent, 2);
                                break;
                            case 1:
                                Intent intent2 = new Intent(getActivity(), ProfileEditActivity.class);
                                intent2.putExtra(ProfileEditActivity.POSITION_INTENT, position);
                                startActivityForResult(intent2, 3);
                                break;
                            case 2:
                                if (items1.get(position).getName() != null) {
                                    List<Profiles.ProfileItem.CommandItem> commands = items1.get(position).getCommands();
                                    if (commands.size() > 0) {
                                        setForegroundText(items1.get(position).getName().toUpperCase());
                                        mDetailsFragment.setText(commands);
                                        showForeground();
                                    } else {
                                        Utils.toast(R.string.profile_empty, getActivity());
                                    }
                                }
                                break;
                            case 3:
                                items1.get(position).enableOnBoot(menuItem.isChecked());
                                mProfiles.commit();
                                return false;
                            case 4:
                                mExportProfile = items1.get(position);
                                requestPermission(0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                break;
                            case 5:
                                mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                                        (dialogInterface, i16) -> {
                                        },
                                        (dialogInterface, i15) -> {
                                            mProfiles.delete(position);
                                            mProfiles.commit();
                                            reload();
                                        },
                                        dialogInterface -> mDeleteDialog = null, getActivity());
                                mDeleteDialog.show();
                                break;
                        }
                        return true;
                    }

                    @Override
                    public void onMenuItemUpdate(de.dlyt.yanndroid.oneui.menu.MenuItem menuItem) {

                    }
                });

            });

            final DescriptionView descriptionView = new DescriptionView();
            descriptionView.setSummary(profileItems.get(i).getName());
            descriptionView.setOnItemClickListener(item -> {
                if (mTaskerMode) {
                    mSelectDialog = ViewUtils.dialogBuilder(getString(R.string.select_question,
                            descriptionView.getSummary()),
                            (dialogInterface, i14) -> {
                            },
                            (dialogInterface, i13) -> ((ProfileTaskerActivity) getActivity()).finish(
                                    descriptionView.getSummary().toString(),
                                    mProfiles.getAllProfiles().get(position).getCommands()),
                            dialogInterface -> mSelectDialog = null, getActivity());
                    mSelectDialog.show();
                } else {
                    mApplyDialog = ViewUtils.dialogBuilder(getString(R.string.apply_question,
                            descriptionView.getSummary()),
                            (dialogInterface, i12) -> {
                            },
                            (dialogInterface, i1) -> {
                                for (Profiles.ProfileItem.CommandItem command : mProfiles.getAllProfiles()
                                        .get(position).getCommands()) {
                                    CPUFreq.ApplyCpu applyCpu;
                                    if (command.getCommand().startsWith("#") && ((applyCpu =
                                            new CPUFreq.ApplyCpu(command.getCommand().substring(1)))
                                            .toString() != null)) {
                                        for (String applyCpuCommand : ApplyOnBoot.getApplyCpu(applyCpu,
                                                RootUtils.getSU())) {
                                            Control.runSetting(applyCpuCommand,
                                                    null, null, null);
                                        }
                                    } else {
                                        Control.runSetting(command.getCommand(),
                                                null, null, null);
                                    }
                                }
                            },
                            dialogInterface -> mApplyDialog = null, getActivity());
                    try {
                        mApplyDialog.show();
                    } catch (NullPointerException ignored) {
                    }
                }
            });

            if (mTaskerMode) {
                items.add(descriptionView);
            } else {
                cardView.addItem(descriptionView);
                items.add(cardView);
            }
        }

        if (!mTaskerMode) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(getActivity(), Widget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.profile_list);
            Tile.publishProfileTile(profileItems, getActivity());
        }
    }

    private Intent createProfileActivityIntent() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);

        NavigationActivity activity = (NavigationActivity) getActivity();
        ArrayList<NavigationActivity.NavigationFragment> fragments = new ArrayList<>();
        boolean add = false;
        for (NavigationActivity.NavigationFragment fragment : activity.getFragments()) {
            if (fragment.mId == R.string.kernel) {
                add = true;
                continue;
            }
            if (!add) continue;
            if (fragment.mFragmentClass == null) break;
            if (activity.getActualFragments().get(fragment.mId) != null) {
                fragments.add(fragment);
            }
        }
        intent.putParcelableArrayListExtra(ProfileActivity.FRAGMENTS_INTENT, fragments);

        return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;
        if (requestCode == 0 || requestCode == 2) {
            LinkedHashMap<String, String> commandsList = new LinkedHashMap<>();
            ArrayList<String> ids = data.getStringArrayListExtra(ProfileActivity.RESULT_ID_INTENT);
            ArrayList<String> commands = data.getStringArrayListExtra(ProfileActivity.RESULT_COMMAND_INTENT);
            for (int i = 0; i < ids.size(); i++) {
                commandsList.put(ids.get(i), commands.get(i));
            }

            if (requestCode == 0) {
                create(commandsList);
            } else {
                Profiles.ProfileItem profileItem = mProfiles.getAllProfiles().get(data.getIntExtra(
                        ProfileActivity.POSITION_INTENT, 0));

                for (Profiles.ProfileItem.CommandItem commandItem : profileItem.getCommands()) {
                    if (ids.contains(commandItem.getPath())) {
                        profileItem.delete(commandItem);
                    }
                }

                for (String path : commandsList.keySet()) {
                    profileItem.putCommand(new Profiles.ProfileItem.CommandItem(
                            path, commandsList.get(path)));
                }
                mProfiles.commit();
            }
        } else if (requestCode == 1) {
            ImportProfile importProfile = new ImportProfile(data.getStringExtra(
                    FilePickerActivity.RESULT_INTENT));

            if (!importProfile.readable()) {
                Utils.toast(R.string.import_malformed, getActivity());
                return;
            }

            if (!importProfile.matchesVersion()) {
                Utils.toast(R.string.import_wrong_version, getActivity());
                return;
            }

            showImportDialog(importProfile);
        } else if (requestCode == 3) {
            reload();
        }
    }

    private void showImportDialog(final ImportProfile importProfile) {
        mImportProfile = importProfile;
        ViewUtils.dialogEditText(null,
                (dialogInterface, i) -> {
                },
                text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }

                    for (Profiles.ProfileItem profileItem : mProfiles.getAllProfiles()) {
                        if (text.equals(profileItem.getName())) {
                            Utils.toast(getString(R.string.already_exists, text), getActivity());
                            return;
                        }
                    }

                    mProfiles.putProfile(text, importProfile.getResults());
                    mProfiles.commit();
                    reload();
                }, getActivity()).setTitle(getString(R.string.name))
                .setOnDismissListener(dialogInterface -> mImportProfile = null).show();
    }

    private void create(final LinkedHashMap<String, String> commands) {
        mCommands = commands;
        ViewUtils.dialogEditText(null,
                (dialogInterface, i) -> {
                },
                text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }

                    for (Profiles.ProfileItem profileItem : mProfiles.getAllProfiles()) {
                        if (text.equals(profileItem.getName())) {
                            Utils.toast(getString(R.string.already_exists, text), getActivity());
                            return;
                        }
                    }

                    mProfiles.putProfile(text, commands);
                    mProfiles.commit();
                    reload();
                }, getActivity())
                .setOnDismissListener(dialogInterface -> mCommands = null)
                .setTitle(getString(R.string.name)).show();
    }

    @Override
    public void onPermissionDenied(int request) {
        super.onPermissionDenied(request);

        if (request == 0) {
            Utils.toast(R.string.permission_denied_write_storage, getActivity());
        }
    }

    @Override
    public void onPermissionGranted(int request) {
        super.onPermissionGranted(request);

        if (request == 0) {
            showExportDialog();
        }
    }

    private void showExportDialog() {
        ViewUtils.dialogEditText(null, (dialogInterface, i) -> {
                },
                text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }

                    if (new ExportProfile(mExportProfile, mProfiles.getVersion()).export(text)) {
                        Utils.toast(getString(R.string.exported_item,
                                text, Utils.getInternalDataStorage()
                                        + "/profiles"), getActivity());
                    } else {
                        Utils.toast(getString(R.string.already_exists, text), getActivity());
                    }
                }, getActivity())
                .setOnDismissListener(dialogInterface -> mExportProfile = null)
                .setTitle(getString(R.string.name)).show();
    }

    public static class DetailsFragment extends BaseFragment {

        private TextView mCodeText;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile_details, container, false);

            mCodeText = rootView.findViewById(R.id.code_text);

            return rootView;
        }

        private void setText(List<Profiles.ProfileItem.CommandItem> commands) {
            StringBuilder commandsText = new StringBuilder();
            for (Profiles.ProfileItem.CommandItem command : commands) {
                CPUFreq.ApplyCpu applyCpu;
                if (command.getCommand().startsWith("#")
                        & ((applyCpu =
                        new CPUFreq.ApplyCpu(command.getCommand().substring(1))).toString() != null)) {
                    for (String applyCpuCommand : ApplyOnBoot.getApplyCpu(applyCpu, RootUtils.getSU())) {
                        commandsText.append(applyCpuCommand).append("\n");
                    }
                } else {
                    commandsText.append(command.getCommand()).append("\n");
                }
            }
            commandsText.setLength(commandsText.length() - 1);

            if (mCodeText != null) {
                mCodeText.setText(commandsText.toString());
            }
        }
    }
}
