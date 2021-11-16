package com.hades.hKtweaks.views.recyclerview;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hades.hKtweaks.R;

import de.dlyt.yanndroid.oneui.view.Switch;

public class SwitcherFView extends RecyclerViewItem {
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
    private String mTitle;
    private String mSummary;
    private boolean mChecked;

    public SwitcherFView(String title, String summary, boolean checked, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        mTitle = title;
        mSummary = summary;
        mChecked = checked;
        mOnCheckedChangeListener = onCheckedChangeListener;
        setFullSpan(true);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_switcher;
    }

    @Override
    public void onCreateView(View view) {
        ((TextView) view.findViewById(R.id.title)).setText(mTitle);
        ((TextView) view.findViewById(R.id.summary)).setText(mSummary);
        Switch mSwitch = view.findViewById(R.id.switcher);
        mSwitch.setChecked(mChecked);
        mSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        view.findViewById(R.id.switcher_frame).setOnClickListener(v -> mSwitch.toggle());
        super.onCreateView(view);
    }

}
