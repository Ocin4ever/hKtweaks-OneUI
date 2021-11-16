package com.hades.hKtweaks.views.recyclerview;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.utils.Utils;

public class DescriptionFView extends RecyclerViewItem {

    private TextView mTitleView;
    private TextView mSummaryView;
    private CharSequence mTitle;
    private CharSequence mSummary;

    private Activity mActivity;

    public DescriptionFView(Activity activity, CharSequence title, CharSequence summary) {
        if (activity == null) {
            throw new IllegalStateException("Activity can't be null");
        }
        mActivity = activity;
        mTitle = title;
        mSummary = summary;
        setFullSpan(true);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_description;
    }

    @Override
    public void onCreateView(View view) {

        mTitleView = view.findViewById(R.id.title);
        mSummaryView = view.findViewById(R.id.summary);

        if (Utils.isTv(mActivity)) {
            mSummaryView.setFocusable(true);
        } else {
            mTitleView.setTextIsSelectable(true);
            mSummaryView.setTextIsSelectable(true);
        }

        mSummaryView.setSelected(true);
        mSummaryView.setMovementMethod(LinkMovementMethod.getInstance());

        super.onCreateView(view);
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        refresh();
    }

    public void setSummary(CharSequence summary) {
        mSummary = summary;
        refresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mTitleView != null) {
            if (mTitle != null) {
                mTitleView.setFocusable(false);
                mTitleView.setText(mTitle);
                mTitleView.setVisibility(View.VISIBLE);
            } else {
                mTitleView.setVisibility(View.GONE);
            }
        }

        if (mSummaryView != null) {
            if (mSummary != null) {
                mSummaryView.setText(mSummary);
                mSummaryView.setVisibility(View.VISIBLE);
            } else {
                mSummaryView.setVisibility(View.GONE);
            }
        }
    }
}
