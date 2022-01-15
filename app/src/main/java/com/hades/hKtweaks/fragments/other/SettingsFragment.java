/*
 * Copyright (C) 2015-2017 Willi Ye <williye97@gmail.com>
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
package com.hades.hKtweaks.fragments.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.util.SeslMisc;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.BaseActivity;
import com.hades.hKtweaks.activities.NavigationActivity;
import com.hades.hKtweaks.activities.SplashActivity;
import com.hades.hKtweaks.services.boot.ApplyOnBootService;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.root.RootUtils;
import com.hades.hKtweaks.views.dialog.Dialog;

import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.layout.PreferenceFragment;
import de.dlyt.yanndroid.oneui.preference.ColorPickerPreference;
import de.dlyt.yanndroid.oneui.preference.HorizontalRadioPreference;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceCategory;
import de.dlyt.yanndroid.oneui.preference.PreferenceGroupAdapter;
import de.dlyt.yanndroid.oneui.preference.PreferenceScreen;
import de.dlyt.yanndroid.oneui.preference.PreferenceViewHolder;
import de.dlyt.yanndroid.oneui.preference.SwitchPreference;
import de.dlyt.yanndroid.oneui.utils.ThemeUtil;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

/**
 * Created by willi on 13.08.16.
 */

public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String KEY_RESET_DATA = "reset_data";
    private static final String KEY_FORCE_ENGLISH = "forceenglish";
    private static final String KEY_OUI4_THEME = "use_oui4_theme";
    private static final String KEY_APPLY_ON_BOOT_TEST = "applyonboottest";
    private static final String KEY_DEBUGGING_CATEGORY = "debugging_category";
    private static final String KEY_LOGCAT = "logcat";
    private static final String KEY_LAST_KMSG = "lastkmsg";
    private static final String KEY_DMESG = "dmesg";
    private static final String KEY_SECURITY_CATEGORY = "security_category";
    private static final String KEY_SET_PASSWORD = "set_password";
    private static final String KEY_DELETE_PASSWORD = "delete_password";
    private static final String KEY_FINGERPRINT = "fingerprint";
    private static final String KEY_SECTIONS = "sections";

    private Preference mFingerprint;

    private String mOldPassword;
    private String mDeletePassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(getResources().getColor(R.color.item_background_color));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOldPassword != null) {
            editPasswordDialog(mOldPassword);
        }
        if (mDeletePassword != null) {
            deletePasswordDialog(mDeletePassword);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        findPreference(KEY_OUI4_THEME).setOnPreferenceChangeListener(this);

        SwitchPreference forceEnglish = (SwitchPreference) findPreference(KEY_FORCE_ENGLISH);
        if (Resources.getSystem().getConfiguration().locale.getLanguage().startsWith("en")) {
            getPreferenceScreen().removePreference(forceEnglish);
        } else {
            forceEnglish.setOnPreferenceChangeListener(this);
        }

        findPreference(KEY_RESET_DATA).setOnPreferenceClickListener(this);
        findPreference(KEY_APPLY_ON_BOOT_TEST).setOnPreferenceClickListener(this);
        findPreference(KEY_LOGCAT).setOnPreferenceClickListener(this);

        if (Utils.existFile("/proc/last_kmsg") || Utils.existFile("/sys/fs/pstore/console-ramoops")) {
            findPreference(KEY_LAST_KMSG).setOnPreferenceClickListener(this);
        } else {
            ((PreferenceCategory) findPreference(KEY_DEBUGGING_CATEGORY)).removePreference(
                    findPreference(KEY_LAST_KMSG));
        }

        findPreference(KEY_DMESG).setOnPreferenceClickListener(this);
        findPreference(KEY_SET_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(KEY_DELETE_PASSWORD).setOnPreferenceClickListener(this);
        findPreference(KEY_DELETE_PASSWORD).setEnabled(!AppSettings.getPassword(getActivity()).isEmpty());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || !FingerprintManagerCompat.from(getActivity()).isHardwareDetected()) {
            ((PreferenceCategory) findPreference(KEY_SECURITY_CATEGORY)).removePreference(
                    findPreference(KEY_FINGERPRINT));
        } else {
            mFingerprint = findPreference(KEY_FINGERPRINT);
            mFingerprint.setEnabled(!AppSettings.getPassword(getActivity()).isEmpty());
        }

        NavigationActivity activity = (NavigationActivity) getActivity();
        PreferenceCategory sectionsCategory = (PreferenceCategory) findPreference(KEY_SECTIONS);
        for (NavigationActivity.NavigationFragment navigationFragment : activity.getFragments()) {
            Class<? extends Fragment> fragmentClass = navigationFragment.mFragmentClass;
            int id = navigationFragment.mId;

            if (fragmentClass != null && fragmentClass != SettingsFragment.class) {
                SwitchPreference switchPreference = new SwitchPreference(getContext());

                switchPreference.setTitle(getString(id));
                switchPreference.setChecked(AppSettings.isFragmentEnabled(fragmentClass, getActivity()));
                switchPreference.setOnPreferenceChangeListener((var1, var2) -> {
                    AppSettings.saveBoolean(String.format("%s_enabled", fragmentClass.getSimpleName()), !((SwitchPreference) var1).isChecked(), getContext());
                    ((NavigationActivity) getActivity()).appendFragments();
                    return true;
                });
                sectionsCategory.addPreference(switchPreference);
            }
        }


        int darkMode = ThemeUtil.getDarkMode(getContext());

        HorizontalRadioPreference darkModePref = (HorizontalRadioPreference) findPreference("dark_mode");
        darkModePref.setOnPreferenceChangeListener(this);
        darkModePref.setDividerEnabled(false);
        darkModePref.setTouchEffectEnabled(false);
        darkModePref.setEnabled(darkMode != ThemeUtil.DARK_MODE_AUTO);
        darkModePref.setValue(SeslMisc.isLightTheme(getContext()) ? "0" : "1");

        SwitchPreference autoDarkModePref = (SwitchPreference) findPreference("dark_mode_auto");
        autoDarkModePref.setOnPreferenceChangeListener(this);
        autoDarkModePref.setChecked(darkMode == ThemeUtil.DARK_MODE_AUTO);

        initThemeColorPicker();

    }

    private void initThemeColorPicker() {
        ColorPickerPreference themeColorPickerPref = (ColorPickerPreference) findPreference("theme_color");
        ArrayList<Integer> recent_colors = new Gson().fromJson(AppSettings.getString("recent_theme_colors", new Gson().toJson(new int[]{getResources().getColor(R.color.primary_color, getContext().getTheme())}), getContext()), new TypeToken<ArrayList<Integer>>() {
        }.getType());
        for (Integer recent_color : recent_colors)
            themeColorPickerPref.onColorChanged(recent_color);

        themeColorPickerPref.setOnPreferenceChangeListener((var1, var2) -> {
            Color color = Color.valueOf((Integer) var2);

            recent_colors.add((Integer) var2);
            AppSettings.saveString("recent_theme_colors", new Gson().toJson(recent_colors), getContext());

            ThemeUtil.setColor((AppCompatActivity) getActivity(), color.red(), color.green(), color.blue());
            return true;
        });
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String key = preference.getKey();
        switch (key) {
            case KEY_OUI4_THEME:
                if ((boolean) o != ((BaseActivity) getActivity()).mUseOUI4Theme)
                    getActivity().recreate();
                return true;
            case KEY_FORCE_ENGLISH:
                getActivity().finish();
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(NavigationActivity.INTENT_SECTION,
                        SettingsFragment.class.getCanonicalName());
                startActivity(intent);
                return true;
            case "dark_mode":
                String currentDarkMode = String.valueOf(ThemeUtil.getDarkMode(getContext()));
                if (currentDarkMode != o) {
                    ThemeUtil.setDarkMode((AppCompatActivity) getActivity(), ((String) o).equals("0") ? ThemeUtil.DARK_MODE_DISABLED : ThemeUtil.DARK_MODE_ENABLED);
                }
                return true;
            case "dark_mode_auto":
                HorizontalRadioPreference darkModePref = (HorizontalRadioPreference) findPreference("dark_mode");
                if ((boolean) o) {
                    darkModePref.setEnabled(false);
                    ThemeUtil.setDarkMode((AppCompatActivity) getActivity(), ThemeUtil.DARK_MODE_AUTO);
                } else {
                    darkModePref.setEnabled(true);
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case KEY_RESET_DATA:
                resetDataDialog();
                return true;
            case KEY_APPLY_ON_BOOT_TEST:
                if (Utils.isServiceRunning(ApplyOnBootService.class, getActivity())) {
                    Utils.toast(R.string.apply_on_boot_running, getActivity());
                } else {
                    Intent intent2 = new Intent(getActivity(), ApplyOnBootService.class);
                    intent2.putExtra("messenger", new Messenger(new MessengerHandler(getActivity())));
                    Utils.startService(getActivity(), intent2);
                }
                return true;
            case KEY_LOGCAT:
                new Execute(getActivity()).execute("logcat -d > /sdcard/logcat.txt");
                return true;
            case KEY_LAST_KMSG:
                if (Utils.existFile("/proc/last_kmsg")) {
                    new Execute(getActivity()).execute("cat /proc/last_kmsg > /sdcard/last_kmsg.txt");
                } else if (Utils.existFile("/sys/fs/pstore/console-ramoops")) {
                    new Execute(getActivity()).execute("cat /sys/fs/pstore/console-ramoops > /sdcard/last_kmsg.txt");
                }
                return true;
            case KEY_DMESG:
                new Execute(getActivity()).execute("dmesg > /sdcard/dmesg.txt");
                return true;
            case KEY_SET_PASSWORD:
                editPasswordDialog(AppSettings.getPassword(getActivity()));
                return true;
            case KEY_DELETE_PASSWORD:
                deletePasswordDialog(AppSettings.getPassword(getActivity()));
                return true;
        }
        return false;
    }

    private void resetDataDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.reset_data_title));
        alert.setMessage(getString(R.string.reset_data_dialog1));
        alert.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
        });
        alert.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
            RootUtils.runCommand("rm -rf /data/.hKtweaks");
            RootUtils.runCommand("pm clear com.hades.hKtweaks");
        });
        alert.show();
    }

    private void editPasswordDialog(final String oldPass) {
        mOldPassword = oldPass;

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        int padding = Math.round(getResources().getDimension(R.dimen.dialog_padding));
        linearLayout.setPadding(padding, padding, padding, padding);

        final AppCompatEditText oldPassword = new AppCompatEditText(getActivity());
        if (!oldPass.isEmpty()) {
            oldPassword.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            oldPassword.setHint(getString(R.string.old_password));
            linearLayout.addView(oldPassword);
        }

        final AppCompatEditText newPassword = new AppCompatEditText(getActivity());
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPassword.setHint(getString(R.string.new_password));
        linearLayout.addView(newPassword);

        final AppCompatEditText confirmNewPassword = new AppCompatEditText(getActivity());
        confirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmNewPassword.setHint(getString(R.string.confirm_new_password));
        linearLayout.addView(confirmNewPassword);

        new Dialog(getActivity()).setView(linearLayout)
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    if (!oldPass.isEmpty() && !oldPassword.getText().toString().equals(Utils
                            .decodeString(oldPass))) {
                        Utils.toast(getString(R.string.old_password_wrong), getActivity());
                        return;
                    }

                    if (newPassword.getText().toString().isEmpty()) {
                        Utils.toast(getString(R.string.password_empty), getActivity());
                        return;
                    }

                    if (!newPassword.getText().toString().equals(confirmNewPassword.getText()
                            .toString())) {
                        Utils.toast(getString(R.string.password_not_match), getActivity());
                        return;
                    }

                    if (newPassword.getText().toString().length() > 32) {
                        Utils.toast(getString(R.string.password_too_long), getActivity());
                        return;
                    }

                    AppSettings.savePassword(Utils.encodeString(newPassword.getText()
                            .toString()), getActivity());
                    if (mFingerprint != null) {
                        mFingerprint.setEnabled(true);
                    }
                    findPreference(KEY_DELETE_PASSWORD).setEnabled(true);
                })
                .setOnDismissListener(dialogInterface -> mOldPassword = null).show();
    }

    private void deletePasswordDialog(final String password) {
        if (password.isEmpty()) {
            Utils.toast(getString(R.string.set_password_first), getActivity());
            return;
        }

        mDeletePassword = password;

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        int padding = Math.round(getResources().getDimension(R.dimen.dialog_padding));
        linearLayout.setPadding(padding, padding, padding, padding);

        final AppCompatEditText mPassword = new AppCompatEditText(getActivity());
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setHint(getString(R.string.password));
        linearLayout.addView(mPassword);

        new Dialog(getActivity()).setView(linearLayout)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    if (!mPassword.getText().toString().equals(Utils.decodeString(password))) {
                        Utils.toast(getString(R.string.password_wrong), getActivity());
                        return;
                    }

                    AppSettings.resetPassword(getActivity());
                    if (mFingerprint != null) {
                        mFingerprint.setEnabled(false);
                    }
                    findPreference(KEY_DELETE_PASSWORD).setEnabled(false);
                })
                .setOnDismissListener(dialogInterface -> mDeletePassword = null).show();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @Override
            public void onBindViewHolder(PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                View icon_frame = holder.itemView.findViewById(R.id.icon_frame);
                if (icon_frame != null) icon_frame.setVisibility(View.GONE);
            }
        };
    }

    private static class MessengerHandler extends Handler {

        private final Context mContext;

        private MessengerHandler(Context context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 1 && mContext != null) {
                Utils.toast(R.string.nothing_apply, mContext);
            }
        }
    }

    private static class Execute extends AsyncTask<String, Void, Void> {
        private ProgressDialog mProgressDialog;

        private Execute(Context context) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getString(R.string.executing));
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setGravity(Gravity.CENTER);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
        }
    }

}
