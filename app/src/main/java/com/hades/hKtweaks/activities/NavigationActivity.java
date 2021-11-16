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
package com.hades.hKtweaks.activities;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.hades.hKtweaks.BuildConfig;
import com.hades.hKtweaks.R;
import com.hades.hKtweaks.fragments.BaseFragment;
import com.hades.hKtweaks.fragments.kernel.BatteryFragment;
import com.hades.hKtweaks.fragments.kernel.BoefflaWakelockFragment;
import com.hades.hKtweaks.fragments.kernel.BusCamFragment;
import com.hades.hKtweaks.fragments.kernel.BusDispFragment;
import com.hades.hKtweaks.fragments.kernel.BusIntFragment;
import com.hades.hKtweaks.fragments.kernel.BusMifFragment;
import com.hades.hKtweaks.fragments.kernel.CPUFragment;
import com.hades.hKtweaks.fragments.kernel.CPUHotplugFragment;
import com.hades.hKtweaks.fragments.kernel.CPUVoltageCl0Fragment;
import com.hades.hKtweaks.fragments.kernel.CPUVoltageCl1Fragment;
import com.hades.hKtweaks.fragments.kernel.DvfsFragment;
import com.hades.hKtweaks.fragments.kernel.EntropyFragment;
import com.hades.hKtweaks.fragments.kernel.GPUFragment;
import com.hades.hKtweaks.fragments.kernel.HmpFragment;
import com.hades.hKtweaks.fragments.kernel.IOFragment;
import com.hades.hKtweaks.fragments.kernel.KSMFragment;
import com.hades.hKtweaks.fragments.kernel.LEDFragment;
import com.hades.hKtweaks.fragments.kernel.LMKFragment;
import com.hades.hKtweaks.fragments.kernel.MiscFragment;
import com.hades.hKtweaks.fragments.kernel.ScreenFragment;
import com.hades.hKtweaks.fragments.kernel.SoundFragment;
import com.hades.hKtweaks.fragments.kernel.SpectrumFragment;
import com.hades.hKtweaks.fragments.kernel.ThermalFragment;
import com.hades.hKtweaks.fragments.kernel.VMFragment;
import com.hades.hKtweaks.fragments.kernel.WakeFragment;
import com.hades.hKtweaks.fragments.kernel.WakelockFragment;
import com.hades.hKtweaks.fragments.other.SettingsFragment;
import com.hades.hKtweaks.fragments.statistics.DeviceFragment;
import com.hades.hKtweaks.fragments.statistics.InputsFragment;
import com.hades.hKtweaks.fragments.statistics.MemoryFragment;
import com.hades.hKtweaks.fragments.statistics.OverallFragment;
import com.hades.hKtweaks.fragments.tools.BackupFragment;
import com.hades.hKtweaks.fragments.tools.BuildpropFragment;
import com.hades.hKtweaks.fragments.tools.InitdFragment;
import com.hades.hKtweaks.fragments.tools.OnBootFragment;
import com.hades.hKtweaks.fragments.tools.ProfileFragment;
import com.hades.hKtweaks.fragments.tools.RecoveryFragment;
import com.hades.hKtweaks.fragments.tools.customcontrols.CustomControlsFragment;
import com.hades.hKtweaks.fragments.tools.downloads.DownloadsFragment;
import com.hades.hKtweaks.services.monitor.Monitor;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Device;
import com.hades.hKtweaks.utils.Updater;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.kernel.battery.Battery;
import com.hades.hKtweaks.utils.kernel.boefflawakelock.BoefflaWakelock;
import com.hades.hKtweaks.utils.kernel.bus.VoltageCam;
import com.hades.hKtweaks.utils.kernel.bus.VoltageDisp;
import com.hades.hKtweaks.utils.kernel.bus.VoltageInt;
import com.hades.hKtweaks.utils.kernel.bus.VoltageMif;
import com.hades.hKtweaks.utils.kernel.cpuhotplug.Hotplug;
import com.hades.hKtweaks.utils.kernel.cpuvoltage.VoltageCl0;
import com.hades.hKtweaks.utils.kernel.cpuvoltage.VoltageCl1;
import com.hades.hKtweaks.utils.kernel.dvfs.Dvfs;
import com.hades.hKtweaks.utils.kernel.entropy.Entropy;
import com.hades.hKtweaks.utils.kernel.gpu.GPU;
import com.hades.hKtweaks.utils.kernel.hmp.Hmp;
import com.hades.hKtweaks.utils.kernel.io.IO;
import com.hades.hKtweaks.utils.kernel.ksm.KSM;
import com.hades.hKtweaks.utils.kernel.led.LED;
import com.hades.hKtweaks.utils.kernel.lmk.LMK;
import com.hades.hKtweaks.utils.kernel.screen.Screen;
import com.hades.hKtweaks.utils.kernel.sound.Sound;
import com.hades.hKtweaks.utils.kernel.spectrum.Spectrum;
import com.hades.hKtweaks.utils.kernel.thermal.Thermal;
import com.hades.hKtweaks.utils.kernel.wake.Wake;
import com.hades.hKtweaks.utils.kernel.wakelock.Wakelock;
import com.hades.hKtweaks.utils.root.RootUtils;
import com.hades.hKtweaks.utils.tools.Backup;
import com.hades.hKtweaks.utils.tools.SupportedDownloads;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;

public class NavigationActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PACKAGE = NavigationActivity.class.getCanonicalName();
    public static final String INTENT_SECTION = PACKAGE + ".INTENT.SECTION";

    private ArrayList<NavigationFragment> mFragments = new ArrayList<>();
    private Map<Integer, Class<? extends Fragment>> mActualFragments = new LinkedHashMap<>();

    private DrawerLayout mDrawer;
    private ToolbarLayout mToolbarLayout;
    private NavigationView mNavigationView;
    private long mLastTimeBackbuttonPressed;

    private int mSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            new FragmentLoader(this).execute();
        } else {
            mFragments = savedInstanceState.getParcelableArrayList("fragments");
            init(savedInstanceState);
        }

        if (AppSettings.getBoolean("show_changelog", true, this)) {
            Utils.changelogDialog(this);
        }

        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                //with root//RootUtils.runCommand("appops set --uid com.hades.hKtweaks MANAGE_EXTERNAL_STORAGE allow");
                startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
            }
        } else {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initFragments() {
        mFragments.clear();
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.statistics));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.overall, OverallFragment.class, R.drawable.ic_samsung_page));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.device, DeviceFragment.class, R.drawable.ic_samsung_device));
        if (Device.MemInfo.getInstance().getItems().size() > 0) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.memory, MemoryFragment.class, R.drawable.ic_samsung_network_storage_manage));
        }
        if (Device.Input.getInstance().supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.inputs, InputsFragment.class, R.drawable.ic_samsung_keyboard));
        }
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.kernel));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.cpu, CPUFragment.class, R.drawable.ic_cpu));
        if (Hotplug.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.cpu_hotplug, CPUHotplugFragment.class, R.drawable.ic_switch));
        }
        if (Hmp.getInstance().supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.hmp, HmpFragment.class, R.drawable.ic_cpu));
        }
        if (Thermal.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.thermal, ThermalFragment.class, R.drawable.ic_temperature));
        }
        if (GPU.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.gpu, GPUFragment.class, R.drawable.ic_samsung_video_conference));
        }
        if (Dvfs.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.dvfs_nav, DvfsFragment.class, R.drawable.ic_dvfs));
        }
        if (Screen.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.screen, ScreenFragment.class, R.drawable.ic_display));
        }
        if (Wake.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.gestures, WakeFragment.class, R.drawable.ic_touch));
        }
        if (Sound.getInstance().supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.sound, SoundFragment.class, R.drawable.ic_samsung_audio));
        }
        if (Spectrum.supported(this)) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.spectrum, SpectrumFragment.class, R.drawable.ic_spectrum_logo));
        }
        if (Battery.getInstance(this).supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.battery, BatteryFragment.class, R.drawable.ic_battery));
        }
        if (LED.getInstance().supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.led, LEDFragment.class, R.drawable.ic_samsung_light_bulb));
        }
        if (IO.getInstance().supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.io_scheduler, IOFragment.class, R.drawable.ic_samsung_sd_card));
        }
        if (KSM.getInstance().supported()) {
            if (KSM.getInstance().isUKSM()) {
                mFragments.add(new NavigationActivity.NavigationFragment(R.string.uksm_name, KSMFragment.class, R.drawable.ic_merge));
            } else {
                mFragments.add(new NavigationActivity.NavigationFragment(R.string.ksm_name, KSMFragment.class, R.drawable.ic_merge));
            }
        }
        if (LMK.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.lmk, LMKFragment.class, R.drawable.ic_samsung_equalizer));
        }
        if (Wakelock.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.wakelock_nav, WakelockFragment.class, R.drawable.ic_samsung_unlock));
        }
        if (BoefflaWakelock.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.boeffla_wakelock, BoefflaWakelockFragment.class, R.drawable.ic_samsung_unlock));
        }
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.virtual_memory, VMFragment.class, R.drawable.ic_samsung_speed));
        if (Entropy.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.entropy, EntropyFragment.class, R.drawable.ic_samsung_devicecare));
        }
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.misc, MiscFragment.class, R.drawable.ic_samsung_apps));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.voltage_control));
        if (VoltageCl1.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.cpucl1_voltage, CPUVoltageCl1Fragment.class, R.drawable.ic_bolt));
        }
        if (VoltageCl0.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.cpucl0_voltage, CPUVoltageCl0Fragment.class, R.drawable.ic_bolt));
        }
        if (VoltageMif.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.busMif_voltage, BusMifFragment.class, R.drawable.ic_bolt));
        }
        if (VoltageInt.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.busInt_voltage, BusIntFragment.class, R.drawable.ic_bolt));
        }
        if (VoltageDisp.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.busDisp_voltage, BusDispFragment.class, R.drawable.ic_bolt));
        }
        if (VoltageCam.supported()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.busCam_voltage, BusCamFragment.class, R.drawable.ic_bolt));
        }
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.tools));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.custom_controls, CustomControlsFragment.class, R.drawable.ic_samsung_plug_in));

        SupportedDownloads supportedDownloads = new SupportedDownloads(this);
        if (supportedDownloads.getLink() != null) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.downloads, DownloadsFragment.class, R.drawable.ic_samsung_download));
        }
        if (Backup.hasBackup()) {
            mFragments.add(new NavigationActivity.NavigationFragment(R.string.backup, BackupFragment.class, R.drawable.ic_samsung_restore));
        }
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.build_prop_editor, BuildpropFragment.class, R.drawable.ic_samsung_edit));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.profile, ProfileFragment.class, R.drawable.ic_samsung_advanced_feature));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.recovery, RecoveryFragment.class, R.drawable.ic_samsung_security));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.initd, InitdFragment.class, R.drawable.ic_shell));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.on_boot, OnBootFragment.class, R.drawable.ic_samsung_list_sort));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.other));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.settings, SettingsFragment.class, R.drawable.ic_samsung_settings));
        mFragments.add(new NavigationActivity.NavigationFragment(R.string.about, Fragment.class, R.drawable.ic_samsung_info));
    }

    private void init(Bundle savedInstanceState) {
        setContentView(R.layout.activity_navigation);

        mDrawer = getDrawerLayout();
        mToolbarLayout = getToolBarLayout();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.clearFocus();
            }
        });

        if (savedInstanceState != null) {
            mSelection = savedInstanceState.getInt(INTENT_SECTION);
        }

        appendFragments(false);
        String section = getIntent().getStringExtra(INTENT_SECTION);
        if (section != null) {
            for (Map.Entry<Integer, Class<? extends Fragment>> entry : mActualFragments.entrySet()) {
                Class<? extends Fragment> fragmentClass = entry.getValue();
                if (fragmentClass != null && fragmentClass.getCanonicalName().equals(section)) {
                    mSelection = entry.getKey();
                    break;
                }
            }
            getIntent().removeExtra(INTENT_SECTION);
        }

        if (mSelection == 0 || mActualFragments.get(mSelection) == null) {
            mSelection = firstTab();
        }
        onItemSelected(mSelection, false);

        if (AppSettings.isDataSharing(this)) {
            startService(new Intent(this, Monitor.class));
        }

        Updater.checkForUpdate(this, new Updater.UpdateChecker() {
            @Override
            public void updateAvailable(boolean available, String url, String versionName) {
                mDrawer.setButtonBadges(available ? ToolbarLayout.N_BADGE : 0, 0);
                if (available)
                    mNavigationView.getMenu().findItem(R.string.about).setActionView(R.layout.sesl_badge);
                else mNavigationView.getMenu().findItem(R.string.about).setActionView(null);
            }

            @Override
            public void githubAvailable(String url) {

            }

            @Override
            public void noConnection() {

            }
        });
    }

    private int firstTab() {
        for (Map.Entry<Integer, Class<? extends Fragment>> entry : mActualFragments.entrySet()) {
            if (entry.getValue() != null) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public void appendFragments() {
        appendFragments(true);
    }

    private void appendFragments(boolean setShortcuts) {
        mActualFragments.clear();
        Menu menu = mNavigationView.getMenu();
        menu.clear();

        SubMenu lastSubMenu = null;
        for (NavigationFragment navigationFragment : mFragments) {
            Class<? extends Fragment> fragmentClass = navigationFragment.mFragmentClass;
            int id = navigationFragment.mId;

            Drawable drawable = ContextCompat.getDrawable(this, navigationFragment.mDrawable != 0 ? navigationFragment.mDrawable : R.drawable.ic_blank);

            if (fragmentClass == null) {
                lastSubMenu = menu.addSubMenu(id);
                mActualFragments.put(id, null);
            } else if (AppSettings.isFragmentEnabled(fragmentClass, this)) {
                MenuItem menuItem = lastSubMenu == null ? menu.add(0, id, 0, id) :
                        lastSubMenu.add(0, id, 0, id);
                menuItem.setIcon(drawable);
                menuItem.setCheckable(true);
                if (mSelection != 0) {
                    mNavigationView.setCheckedItem(mSelection);
                }
                mActualFragments.put(id, fragmentClass);
            }
        }
        if (setShortcuts) {
            setShortcuts();
        }
    }

    private NavigationFragment findNavigationFragmentByClass(Class<? extends Fragment> fragmentClass) {
        if (fragmentClass == null) return null;
        for (NavigationFragment navigationFragment : mFragments) {
            if (fragmentClass == navigationFragment.mFragmentClass) {
                return navigationFragment;
            }
        }
        return null;
    }

    private void setShortcuts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;

        PriorityQueue<Class<? extends Fragment>> queue = new PriorityQueue<>(
                (o1, o2) -> {
                    int opened1 = AppSettings.getFragmentOpened(o1, this);
                    int opened2 = AppSettings.getFragmentOpened(o2, this);
                    return opened2 - opened1;
                });

        for (Map.Entry<Integer, Class<? extends Fragment>> entry : mActualFragments.entrySet()) {
            Class<? extends Fragment> fragmentClass = entry.getValue();
            if (fragmentClass == null || fragmentClass == SettingsFragment.class) continue;

            queue.offer(fragmentClass);
        }

        List<ShortcutInfo> shortcutInfos = new ArrayList<>();
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        shortcutManager.removeAllDynamicShortcuts();
        for (int i = 0; i < 4; i++) {
            NavigationFragment fragment = findNavigationFragmentByClass(queue.poll());
            if (fragment == null || fragment.mFragmentClass == null) continue;
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(INTENT_SECTION, fragment.mFragmentClass.getCanonicalName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(this,
                    fragment.mFragmentClass.getSimpleName())
                    .setShortLabel(getString(fragment.mId))
                    .setLongLabel(Utils.strFormat(getString(R.string.open), getString(fragment.mId)))
                    .setIcon(Icon.createWithResource(this, fragment.mDrawable == 0 ?
                            R.drawable.ic_blank : fragment.mDrawable))
                    .setIntent(intent)
                    .build();
            shortcutInfos.add(shortcut);
        }
        shortcutManager.setDynamicShortcuts(shortcutInfos);
    }

    public ArrayList<NavigationFragment> getFragments() {
        return mFragments;
    }

    public Map<Integer, Class<? extends Fragment>> getActualFragments() {
        return mActualFragments;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && ((androidx.drawerlayout.widget.DrawerLayout) mDrawer.findViewById(R.id.drawerLayout)).isDrawerOpen(GravityCompat.START)) {
            mDrawer.setDrawerOpen(false, true);
        } else {
            Fragment currentFragment = getFragment(mSelection);
            if (!(currentFragment instanceof BaseFragment)
                    || !((BaseFragment) currentFragment).onBackPressed()) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - mLastTimeBackbuttonPressed > 2000) {
                    mLastTimeBackbuttonPressed = currentTime;
                    Utils.toast(R.string.press_back_again_exit, this);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int id : mActualFragments.keySet()) {
            Fragment fragment = fragmentManager.findFragmentByTag(id + "_key");
            if (fragment != null) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.commitAllowingStateLoss();
        RootUtils.closeSU();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("fragments", mFragments);
        outState.putInt(INTENT_SECTION, mSelection);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.string.about) {
            startActivity(new Intent().setClass(this, AboutActivity.class));
            return false;
        }

        onItemSelected(item.getItemId(), true);
        return true;
    }

    private void onItemSelected(final int res, boolean saveOpened) {
        mToolbarLayout.setTitle(getString(res));
        mToolbarLayout.setSubtitle(null);

        mDrawer.setDrawerOpen(false, true);
        mNavigationView.setCheckedItem(res);
        mSelection = res;
        final Fragment fragment = getFragment(res);

        if (saveOpened) {
            AppSettings.saveFragmentOpened(fragment.getClass(),
                    AppSettings.getFragmentOpened(fragment.getClass(), this) + 1,
                    this);
        }
        setShortcuts();

        mDrawer.postDelayed(()
                        -> {
                    getSupportFragmentManager().beginTransaction().replace(
                            R.id.content_frame, fragment, res + "_key").commitAllowingStateLoss();
                },
                250);
    }

    private Fragment getFragment(int res) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(res + "_key");
        if (fragment == null && mActualFragments.containsKey(res)) {
            fragment = Fragment.instantiate(this,
                    mActualFragments.get(res).getCanonicalName());
        }
        return fragment;
    }

    private static class FragmentLoader extends AsyncTask<Void, Void, Void> {

        private WeakReference<NavigationActivity> mRefActivity;

        private FragmentLoader(NavigationActivity activity) {
            mRefActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NavigationActivity activity = mRefActivity.get();
            if (activity == null) return null;
            activity.initFragments();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            NavigationActivity activity = mRefActivity.get();
            if (activity == null) return;
            activity.init(null);
        }
    }

    public static class NavigationFragment implements Parcelable {

        public static final Creator CREATOR = new Creator<NavigationFragment>() {
            @Override
            public NavigationFragment createFromParcel(Parcel source) {
                return new NavigationFragment(source);
            }

            @Override
            public NavigationFragment[] newArray(int size) {
                return new NavigationFragment[0];
            }
        };
        public int mId;
        public Class<? extends Fragment> mFragmentClass;
        private int mDrawable;

        NavigationFragment(int id) {
            this(id, null, 0);
        }

        NavigationFragment(int id, Class<? extends Fragment> fragment, int drawable) {
            mId = id;
            mFragmentClass = fragment;
            mDrawable = drawable;
        }

        NavigationFragment(Parcel parcel) {
            mId = parcel.readInt();
            mFragmentClass = (Class<? extends Fragment>) parcel.readSerializable();
            mDrawable = parcel.readInt();
        }

        @Override
        public String toString() {
            return String.valueOf(mId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mId);
            dest.writeSerializable(mFragmentClass);
            dest.writeInt(mDrawable);
        }
    }

}
