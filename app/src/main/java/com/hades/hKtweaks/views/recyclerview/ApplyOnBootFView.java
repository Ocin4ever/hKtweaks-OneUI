package com.hades.hKtweaks.views.recyclerview;

import android.app.Activity;
import android.view.View;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.fragments.ApplyOnBootFragment;
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
import com.hades.hKtweaks.fragments.kernel.ThermalFragment;
import com.hades.hKtweaks.fragments.kernel.VMFragment;
import com.hades.hKtweaks.fragments.kernel.WakeFragment;
import com.hades.hKtweaks.fragments.kernel.WakelockFragment;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.AppSettings;

import java.util.HashMap;

import de.dlyt.yanndroid.oneui.view.SwitchBar;

public class ApplyOnBootFView extends RecyclerViewItem {

    public static final String CPU = "cpu_onboot";
    public static final String CPU_CL0_VOLTAGE = "cpucl0voltage_onboot";
    public static final String CPU_CL1_VOLTAGE = "cpucl1voltage_onboot";
    public static final String CPU_HOTPLUG = "cpuhotplug_onboot";
    public static final String BUS_MIF = "busMif_onboot";
    public static final String BUS_INT = "busInt_onboot";
    public static final String BUS_CAM = "busCam_onboot";
    public static final String BUS_DISP = "busDisp_onboot";
    public static final String HMP = "hmp_onboot";
    public static final String THERMAL = "thermal_onboot";
    public static final String GPU = "gpu_onboot";
    public static final String DVFS = "dvfs_onboot";
    public static final String SCREEN = "screen_onboot";
    public static final String WAKE = "wake_onboot";
    public static final String SOUND = "sound_onboot";
    public static final String BATTERY = "battery_onboot";
    public static final String LED = "led_onboot";
    public static final String IO = "io_onboot";
    public static final String KSM = "ksm_onboot";
    public static final String LMK = "lmk_onboot";
    public static final String WAKELOCK = "wakelock_onboot";
    public static final String BOEFFLA_WAKELOCK = "boeffla_wakelock_onboot";
    public static final String VM = "vm_onboot";
    public static final String ENTROPY = "entropy_onboot";
    public static final String MISC = "misc_onboot";
    private static final String PACKAGE = ApplyOnBootFragment.class.getCanonicalName();
    private static final String INTENT_CATEGORY = PACKAGE + ".INTENT.CATEGORY";
    private static final HashMap<Class, String> sAssignments = new HashMap<>();

    static {
        sAssignments.put(CPUFragment.class, CPU);
        sAssignments.put(CPUVoltageCl0Fragment.class, CPU_CL0_VOLTAGE);
        sAssignments.put(CPUVoltageCl1Fragment.class, CPU_CL1_VOLTAGE);
        sAssignments.put(CPUHotplugFragment.class, CPU_HOTPLUG);
        sAssignments.put(BusMifFragment.class, BUS_MIF);
        sAssignments.put(BusIntFragment.class, BUS_INT);
        sAssignments.put(BusCamFragment.class, BUS_CAM);
        sAssignments.put(BusDispFragment.class, BUS_DISP);
        sAssignments.put(HmpFragment.class, HMP);
        sAssignments.put(ThermalFragment.class, THERMAL);
        sAssignments.put(GPUFragment.class, GPU);
        sAssignments.put(DvfsFragment.class, DVFS);
        sAssignments.put(ScreenFragment.class, SCREEN);
        sAssignments.put(WakeFragment.class, WAKE);
        sAssignments.put(SoundFragment.class, SOUND);
        sAssignments.put(BatteryFragment.class, BATTERY);
        sAssignments.put(LEDFragment.class, LED);
        sAssignments.put(IOFragment.class, IO);
        sAssignments.put(KSMFragment.class, KSM);
        sAssignments.put(LMKFragment.class, LMK);
        sAssignments.put(WakelockFragment.class, WAKELOCK);
        sAssignments.put(BoefflaWakelockFragment.class, BOEFFLA_WAKELOCK);
        sAssignments.put(VMFragment.class, VM);
        sAssignments.put(EntropyFragment.class, ENTROPY);
        sAssignments.put(MiscFragment.class, MISC);
    }

    private Activity mActivity;
    private RecyclerViewFragment mRecyclerViewFragment;

    public ApplyOnBootFView(Activity activity, RecyclerViewFragment recyclerViewFragment) {
        if (activity == null) {
            throw new IllegalStateException("Activity can't be null");
        }
        mActivity = activity;
        mRecyclerViewFragment = recyclerViewFragment;
        setFullSpan(true);
    }

    public static String getAssignment(Class fragment) {
        if (!sAssignments.containsKey(fragment)) {
            throw new RuntimeException("Assignment key does not exists: " + fragment.getSimpleName());
        }
        return sAssignments.get(fragment);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_apply_on_boot;
    }

    @Override
    public void onCreateView(View view) {
        final String category = getAssignment(mRecyclerViewFragment.getClass());
        SwitchBar switcher = view.findViewById(R.id.switcher);
        switcher.setSwitchBarText(R.string.apply_on_boot, R.string.apply_on_boot);
        switcher.setChecked(AppSettings.getBoolean(category, false, mActivity));
        switcher.addOnSwitchChangeListener((switchCompat, z) -> AppSettings.saveBoolean(category, z, mActivity));

        super.onCreateView(view);
    }

}
