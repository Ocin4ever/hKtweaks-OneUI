package com.hades.hKtweaks.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.utils.Utils;

import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslLinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class AboutInfoActivity extends BaseActivity {

    private ToolbarLayout toolbarLayout;
    private RecyclerView recyclerView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_info);
        mContext = this;

        toolbarLayout = getToolBarLayout();
        toolbarLayout.setNavigationButtonTooltip(getText(R.string.sesl_navigate_up));
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());
        recyclerView = findViewById(R.id.info_recycler_view);
        recyclerView.setLayoutManager(new SeslLinearLayoutManager(this));

        switch (getIntent().getStringExtra("category")) {
            case "devs":
                toolbarLayout.setTitle(getString(R.string.developers));
                recyclerView.setAdapter(new DevsAdapter());
                break;
            case "libs":
                toolbarLayout.setTitle(getString(R.string.libraries));
                recyclerView.setAdapter(new LibsAdapter());
                break;
        }

        recyclerView.seslSetFillBottomEnabled(true);
        recyclerView.seslSetLastRoundedCorner(true);
        recyclerView.addItemDecoration(new ItemDecoration());
    }

    private class DevsAdapter extends RecyclerView.Adapter<DevsAdapter.ViewHolder> {

        private ArrayList<Dev> devs;

        public DevsAdapter() {
            super();
            devs = new ArrayList<>();
            devs.add(new Dev(getDrawable(R.drawable.ic_profile_grarak), getString(R.string.about_me_grarak), "https://github.com/Grarak", null));
            devs.add(new Dev(getDrawable(R.drawable.ic_profile_morogoku), getString(R.string.about_me_morogoku), "https://github.com/morogoku", "https://www.paypal.me/morogoku"));
            devs.add(new Dev(getDrawable(R.drawable.ic_profile_corsicanu), getString(R.string.about_me_corsicanu), "https://github.com/corsicanu", "https://www.paypal.me/corsicanu"));
            devs.add(new Dev(getDrawable(R.drawable.ic_profile_yanndroid), getString(R.string.about_me_yanndroid), "https://github.com/Yanndroid", null));
        }

        @Override
        public int getItemCount() {
            return devs.size();
        }

        @Override
        public void onBindViewHolder(DevsAdapter.ViewHolder var1, int var2) {
            var1.dev_logo.setImageDrawable(devs.get(var2).logo);
            var1.dev_logo.setClipToOutline(true);
            var1.dev_description.setText(devs.get(var2).description);

            var1.dev_donate.setVisibility(devs.get(var2).donation == null ? View.GONE : View.VISIBLE);
            var1.dev_donate.setOnClickListener(v -> Utils.launchUrl(devs.get(var2).donation, mContext));

            var1.dev_github.setVisibility(devs.get(var2).github == null ? View.GONE : View.VISIBLE);
            var1.dev_github.setOnClickListener(v -> Utils.launchUrl(devs.get(var2).github, mContext));
        }

        @Override
        public DevsAdapter.ViewHolder onCreateViewHolder(ViewGroup var1, int var2) {
            return new DevsAdapter.ViewHolder(getLayoutInflater().inflate(R.layout.dev_list_item, var1, false));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView dev_logo;
            ImageView dev_donate;
            ImageView dev_github;
            TextView dev_description;

            public ViewHolder(View var1) {
                super(var1);
                dev_logo = var1.findViewById(R.id.dev_logo);
                dev_donate = var1.findViewById(R.id.dev_donate);
                dev_github = var1.findViewById(R.id.dev_github);
                dev_description = var1.findViewById(R.id.dev_description);
            }
        }

        class Dev {
            Drawable logo;
            String description;
            String donation;
            String github;

            Dev(Drawable logo, String description, String github, String donation) {
                this.logo = logo;
                this.description = description;
                this.github = github;
                this.donation = donation;
            }
        }

    }

    private class LibsAdapter extends RecyclerView.Adapter<LibsAdapter.ViewHolder> {

        private ArrayList<Lib> libs;

        public LibsAdapter() {
            super();
            libs = new ArrayList<>();
            libs.add(new Lib("Yanndroid  •  OneUI Design Library", "https://github.com/Yanndroid/OneUI-Design-Library"));
            libs.add(new Lib("Roman Nurik  •  DashClock", "https://github.com/romannurik/dashclock"));
            libs.add(new Lib("CyanogenMod  •  CyanogenMod Platform SDK", "https://github.com/CyanogenMod/cm_platform_sdk"));
            libs.add(new Lib("Bumptech  •  Glide", "https://github.com/bumptech/glide"));
            libs.add(new Lib("Bvalosek  •  CpuSpy", "https://github.com/bvalosek/cpuspy"));
            libs.add(new Lib("Grouxho  •  Grx SoundControl", "https://github.com/Grouxho"));
            libs.add(new Lib("Google  •  Gson", "https://github.com/google/gson"));
            libs.add(new Lib("Google  •  Firebase", "https://firebase.google.com/"));
            libs.add(new Lib("Google  •  AppCompat", "https://developer.android.com/topic/libraries/support-library/features.html#v7"));
            libs.add(new Lib("Google  •  Material", "https://github.com/material-components/material-components-android"));
        }

        @Override
        public int getItemCount() {
            return libs.size();
        }

        @Override
        public void onBindViewHolder(LibsAdapter.ViewHolder var1, int var2) {
            var1.lib_name.setText(libs.get(var2).name);
            var1.lib_name.setOnClickListener(v -> Utils.launchUrl(libs.get(var2).url, mContext));

        }

        @Override
        public LibsAdapter.ViewHolder onCreateViewHolder(ViewGroup var1, int var2) {
            return new LibsAdapter.ViewHolder(getLayoutInflater().inflate(R.layout.lib_list_item, var1, false));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView lib_name;

            public ViewHolder(View var1) {
                super(var1);
                lib_name = var1.findViewById(R.id.lib_name);
            }
        }

        class Lib {
            String name;
            String url;

            Lib(String name, String url) {
                this.name = name;
                this.url = url;
            }
        }
    }

    public class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void seslOnDispatchDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.seslOnDispatchDraw(canvas, recyclerView, state);
            TypedValue divider = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);
            Drawable mDivider = mContext.getDrawable(divider.resourceId);
            if (mDivider == null) return;

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View childAt = recyclerView.getChildAt(i);
                int y = ((int) childAt.getY()) + childAt.getHeight();
                mDivider.setBounds(0, y, recyclerView.getWidth(), mDivider.getIntrinsicHeight() + y);
                mDivider.draw(canvas);
            }
        }
    }

}