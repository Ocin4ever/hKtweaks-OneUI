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
package com.hades.hKtweaks.activities.tools.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.BaseActivity;
import com.hades.hKtweaks.activities.NavigationActivity;
import com.hades.hKtweaks.database.Settings;
import com.hades.hKtweaks.fragments.ApplyOnBootFragment;
import com.hades.hKtweaks.fragments.BaseFragment;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.root.Control;
import com.hades.hKtweaks.views.dialog.Dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.view.TabLayout;
import de.dlyt.yanndroid.oneui.view.ViewPager;

/**
 * Created by willi on 11.07.16.
 */
public class ProfileActivity extends BaseActivity {

    public static final String POSITION_INTENT = "position";
    public static final String FRAGMENTS_INTENT = "fragments";
    public static final String RESULT_ID_INTENT = "result_id";
    public static final String RESULT_COMMAND_INTENT = "result_command";

    private LinkedHashMap<String, Fragment> mItems = new LinkedHashMap<>();

    private int mProfilePosition;
    private int mMode;
    private boolean mHideWarningDialog;
    private int mCurPosition;

    @Override
    protected void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        ArrayList<NavigationActivity.NavigationFragment> fragments =
                intent.getParcelableArrayListExtra(FRAGMENTS_INTENT);

        for (NavigationActivity.NavigationFragment navigationFragment : fragments) {
            mItems.put(getString(navigationFragment.mId), getFragment(navigationFragment.mId,
                    navigationFragment.mFragmentClass));
        }
        mItems.remove("Spectrum");

        if (mItems.size() < 1) {
            Utils.toast(R.string.sections_disabled, this);
            finish();
            return;
        }

        mProfilePosition = intent.getIntExtra(POSITION_INTENT, -1);
        if (savedInstanceState != null && (mMode = savedInstanceState.getInt("mode")) != 0) {
            if (mMode == 1) {
                initNewMode(savedInstanceState);
            } else {
                currentSettings();
            }
        } else {
            new Dialog(this).setItems(getResources().getStringArray(R.array.profile_modes),
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                initNewMode(savedInstanceState);
                                break;
                            case 1:
                                currentSettings();
                                break;
                        }
                    }).setCancelable(false).show();
        }
    }

    public Fragment getFragment(int res, Class<? extends Fragment> fragmentClass) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(res + "_key");
        if (fragment == null) {
            fragment = Fragment.instantiate(this, fragmentClass.getCanonicalName());
        }
        return fragment;
    }

    private void initNewMode(Bundle savedInstanceState) {
        mMode = 1;
        setContentView(R.layout.activity_profile);

        Control.clearProfileCommands();
        Control.setProfileMode(true);

        ToolbarLayout toolbarLayout = getToolBarLayout();
        toolbarLayout.setTitle(getString(R.string.profile));
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());
        toolbarLayout.inflateToolbarMenu(R.menu.save_menu);
        toolbarLayout.setOnToolbarMenuItemClickListener(item -> returnIntent(Control.getProfileCommands()));

        final ViewPager viewPager = findViewById(R.id.viewpager);

        if (savedInstanceState != null) {
            mHideWarningDialog = savedInstanceState.getBoolean("hidewarningdialog");
        }
        if (!mHideWarningDialog) {
            ViewUtils.dialogBuilder(getString(R.string.profile_warning), null,
                    (dialogInterface, i) -> {
                    }, dialog -> mHideWarningDialog = true, this).show();
        }

        viewPager.setOffscreenPageLimit(mItems.size());
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurPosition = position;
            }

            @Override
            public void onPageSelected(int position) {
                mCurPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.updateWidget();
    }

    private void currentSettings() {
        mMode = 2;
        //ViewUtils.showDialog(getSupportFragmentManager(), CurrentSettingsFragment.newInstance(mItems));
        CurrentSettingsFragment currentSettingsFragment = new CurrentSettingsFragment(mItems);
        currentSettingsFragment.setCancelable(false);
        currentSettingsFragment.setRetainInstance(true);
        currentSettingsFragment.show(getSupportFragmentManager(), "");
    }

    private void returnIntent(LinkedHashMap<String, String> commandsList) {
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> commands = new ArrayList<>();
        Collections.addAll(ids, commandsList.keySet().toArray(new String[commands.size()]));
        Collections.addAll(commands, commandsList.values().toArray(new String[commands.size()]));
        if (commands.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra(POSITION_INTENT, mProfilePosition);
            intent.putExtra(RESULT_ID_INTENT, ids);
            intent.putExtra(RESULT_COMMAND_INTENT, commands);
            setResult(0, intent);
            finish();
        } else {
            Utils.toast(R.string.no_changes, ProfileActivity.this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mMode);
        outState.putBoolean("hidewarningdialog", mHideWarningDialog);
    }

    @Override
    public void finish() {
        Control.setProfileMode(false);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = (BaseFragment) mItems.values().toArray(new Fragment[mItems.size()])[mCurPosition];
        if (!fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public static class CurrentSettingsFragment extends DialogFragment {

        private LinkedHashMap<String, Fragment> mList;

        public CurrentSettingsFragment(LinkedHashMap<String, Fragment> mList) {
            this.mList = mList;
        }

        @Override
        public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
            View rootView = getLayoutInflater().inflate(R.layout.fragment_profile_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(rootView);

            LinearLayout checkBoxParent = rootView.findViewById(R.id.checkbox_parent);
            final HashMap<CheckBox, Class> checkBoxes = new HashMap<>();
            for (final String name : mList.keySet()) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                checkBox.setBackgroundResource(R.drawable.sesl_control_background);
                checkBox.setButtonDrawable(R.drawable.sesl_btn_check);
                checkBox.setText(name);
                checkBoxParent.addView(checkBox);

                checkBoxes.put(checkBox, mList.get(name).getClass());
            }

            ((CheckBox) rootView.findViewById(R.id.select_all)).setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CheckBox compatCheckBox : checkBoxes.keySet()) {
                    compatCheckBox.setChecked(isChecked);
                }
            });

            Button cancel = rootView.findViewById(R.id.cancel);
            cancel.setOnClickListener(v -> getActivity().finish());

            Button done = rootView.findViewById(R.id.done);
            done.setOnClickListener(v -> {
                List<String> categories = new ArrayList<>();
                for (CheckBox compatCheckBox : checkBoxes.keySet()) {
                    if (compatCheckBox.isChecked()) {
                        categories.add(ApplyOnBootFragment.getAssignment(checkBoxes.get(compatCheckBox)));
                    }
                }
                if (categories.size() < 1) {
                    Utils.toast(R.string.nothing_selected, getActivity());
                    return;
                }

                LinkedHashMap<String, String> items = new LinkedHashMap<>();
                List<Settings.SettingsItem> settingsItems = new Settings(getActivity()).getAllSettings();
                for (Settings.SettingsItem item : settingsItems) {
                    if (categories.contains(item.getCategory())) {
                        items.put(item.getId(), item.getSetting());
                    }
                }
                ((ProfileActivity) getActivity()).returnIntent(items);
            });

            return builder.create();
        }

    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private final LinkedHashMap<String, Fragment> mFragments;

        private PagerAdapter(FragmentManager fm, LinkedHashMap<String, Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(mFragments.keySet().toArray(new String[mFragments.size()])[position]);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragments.keySet().toArray(new String[mFragments.size()])[position];
        }
    }

}
