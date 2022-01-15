package com.grx.soundcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.utils.kernel.sound.MoroSound;

import de.dlyt.yanndroid.oneui.view.SeekBar;


public class GrxEqualizerBandController extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    public int mBandId;
    public VerticalSeekBar mVerticalSeekBar;
    public AppCompatTextView mValueTextView;
    EqBandValueChange mCallBack = null;
    String mCurrentValue, mOldValue;

    public GrxEqualizerBandController(Context context) {
        this(context, null, 0);
    }

    public GrxEqualizerBandController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GrxEqualizerBandController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setCallBack(EqBandValueChange listener) {
        mCallBack = listener;
    }

    private void initView() {
        mBandId = Integer.valueOf((String) getTag());

        inflate(getContext(), R.layout.grx_equalizer_band, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mVerticalSeekBar = findViewById(R.id.eqseekbar);
        mVerticalSeekBar.grxSetInitialized(false);
        mVerticalSeekBar.setOnSeekBarChangeListener(this);
        mValueTextView = findViewById(R.id.value);

        TextView bandview = findViewById(R.id.band);
        String[] bands = getResources().getStringArray(R.array.equalizerbands);
        bandview.setText(String.valueOf(bands[mBandId]));
        updateSeekBar();
    }

    public void updateSeekBar() {
        mVerticalSeekBar.setTickMark(getContext().getDrawable(R.drawable.eq_tick_mark));
        mVerticalSeekBar.grxSetCurrentKernelValue(mBandId);
        mVerticalSeekBar.grxSetInitialized(true);
        mValueTextView.setText(mVerticalSeekBar.grxGetNormalizedProgress() + " dB");

        mCurrentValue = MoroSound.getEqValue(mBandId);
        if (mCurrentValue == null || mCurrentValue.isEmpty()) mCurrentValue = "0";
        mOldValue = mCurrentValue;
        mVerticalSeekBar.grxSetSeekBarProgress(mCurrentValue);
    }

    /************** seekbar listener ***********/

    public void onStopTrackingTouch(SeekBar seekBar) {
        mCurrentValue = ((VerticalSeekBar) seekBar).grxGetNormalizedStringProgress();
        if (mCurrentValue == null || mCurrentValue.isEmpty()) mCurrentValue = "0";
        if (mCurrentValue.equals(mOldValue)) return;
        mOldValue = mCurrentValue;
        if (mCallBack != null) mCallBack.EqValueChanged(mBandId, mCurrentValue);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mOldValue = mCurrentValue;
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String newvalue = ((VerticalSeekBar) seekBar).grxGetNormalizedStringProgress();

        if (newvalue != null) {
            mValueTextView.setText(newvalue + "dB");
            MoroSound.setEqValues(newvalue, mBandId, getContext());
        }
    }

    public interface EqBandValueChange {
        void EqValueChanged(int id, String value);
    }
}
