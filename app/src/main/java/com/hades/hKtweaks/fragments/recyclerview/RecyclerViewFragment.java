/*
 * Copyright (C) 2015-2018 Willi Ye <williye97@gmail.com>
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
package com.hades.hKtweaks.fragments.recyclerview;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.BaseActivity;
import com.hades.hKtweaks.activities.tools.profile.ProfileActivity;
import com.hades.hKtweaks.fragments.BaseFragment;
import com.hades.hKtweaks.fragments.LoadingFragment;
import com.hades.hKtweaks.utils.Log;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewAdapter;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.menu.Menu;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

/**
 * Created by willi on 16.04.16.
 */
public abstract class RecyclerViewFragment extends BaseFragment {

    AsyncTask<Void, Void, List<RecyclerViewItem>> mReloader;
    AsyncTask<Void, Void, Void> mDialogLoader;
    private Handler mHandler;
    private ScheduledThreadPoolExecutor mPoolExecutor;
    private View mRootView;
    private List<RecyclerViewItem> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private View mProgress;
    private AsyncTask<Void, Void, List<RecyclerViewItem>> mLoader;
    private Animation mSlideInOutAnimation;

    private Fragment mForegroundFragment;
    private View mForegroundParent;
    private TextView mForegroundText;
    private CharSequence mForegroundStrText;

    private Fragment mDialogFragment;
    private View mDialogParent;
    private Runnable mScheduler = () -> {
        refreshThread();

        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (isAdded()) {
                refresh();
            }
        });
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(getActivity() instanceof ProfileActivity)) initToolbarMenu();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mHandler = new Handler();

        mRecyclerView = mRootView.findViewById(R.id.recyclerview);
        mProgress = mRootView.findViewById(R.id.progress);

        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.setAdapter(mRecyclerViewAdapter == null ? mRecyclerViewAdapter = new RecyclerViewAdapter(mItems, null) : mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        if (mForegroundFragment == null) {
            mForegroundFragment = getForegroundFragment();
        }
        if (mForegroundFragment != null) {
            mForegroundParent = mRootView.findViewById(R.id.foreground_parent);
            mForegroundText = mRootView.findViewById(R.id.foreground_text);
            getChildFragmentManager().beginTransaction().replace(R.id.foreground_content,
                    mForegroundFragment).commit();
            mForegroundParent.setOnClickListener(v -> dismissForeground());
        }

        if (mDialogFragment == null) {
            mDialogFragment = getDialogFragment();
        }
        if (mDialogFragment != null) {
            mDialogParent = mRootView.findViewById(R.id.dialog_parent);
            getChildFragmentManager().beginTransaction().replace(R.id.dialog_content,
                    mDialogFragment).commit();
            if (mDialogLoader != null) {
                mDialogParent.setVisibility(View.VISIBLE);
            }
        }

        if (itemsSize() == 0) {
            mLoader = new LoaderTask(this, savedInstanceState);
            mLoader.execute();
        } else {
            showProgress();
            init();
            hideProgress();
            postInit();
        }

        return mRootView;
    }

    protected <T extends RecyclerViewFragment> void reload(ReloadHandler<T> listener) {
        if (mReloader == null) {
            mReloader = new LoadAsyncTask<>((T) this, listener);
            mReloader.execute();
        }
    }

    protected void load(List<RecyclerViewItem> items) {
    }

    protected void init() {
    }

    protected void postInit() {
        if (getActivity() != null && isAdded()) {
            for (RecyclerViewItem item : mItems) {
                item.onRecyclerViewCreate(getActivity());
            }
        }
    }

    protected abstract void addItems(List<RecyclerViewItem> items);

    protected void addItem(RecyclerViewItem recyclerViewItem) {
        mItems.add(recyclerViewItem);
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyItemInserted(mItems.size() - 1);
        }
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) mLayoutManager).setSpanCount(getSpanCount());
        }
    }

    protected void setToolbarTitle(String title, String subtitle) {
        if (getActivity() instanceof BaseActivity) {
            ToolbarLayout toolbarLayout = ((BaseActivity) getActivity()).getToolBarLayout();
            toolbarLayout.setTitle(title);
            toolbarLayout.setSubtitle(subtitle);
        } else Log.e("not instanceof BaseActivity");
    }

    private void initToolbarMenu() {
        if (getActivity() instanceof BaseActivity) {
            ToolbarLayout toolbarLayout = ((BaseActivity) getActivity()).getToolBarLayout();
            toolbarLayout.inflateToolbarMenu(R.menu.rvf_menu);
            hideToolbarActionButton();
        } else Log.e("not instanceof BaseActivity");
    }

    protected void showToolbarActionButton(ToolbarLayout.OnMenuItemClickListener listener, @IdRes int... id) {
        if (getActivity() instanceof BaseActivity) {
            ToolbarLayout toolbarLayout = ((BaseActivity) getActivity()).getToolBarLayout();
            Menu toolbarMenu = toolbarLayout.getToolbarMenu();
            if (toolbarMenu == null) initToolbarMenu();
            for (int i : id) toolbarMenu.findItem(i).setVisible(true);
            toolbarLayout.setOnToolbarMenuItemClickListener(listener);
        } else Log.e("not instanceof BaseActivity");
    }

    protected void hideToolbarActionButton() {
        if (getActivity() instanceof BaseActivity) {
            ToolbarLayout toolbarLayout = ((BaseActivity) getActivity()).getToolBarLayout();
            Menu toolbarMenu = toolbarLayout.getToolbarMenu();
            toolbarMenu.findItem(R.id.menu_search).setVisible(false);
            toolbarMenu.findItem(R.id.menu_add).setVisible(false);
            toolbarMenu.findItem(R.id.menu_done).setVisible(false);
            toolbarLayout.setOnToolbarMenuItemClickListener(item -> true);
        } else Log.e("not instanceof BaseActivity");
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
    }

    protected void removeItem(RecyclerViewItem recyclerViewItem) {
        int position = mItems.indexOf(recyclerViewItem);
        if (position >= 0) {
            mItems.remove(position);
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.notifyItemRemoved(position);
                mRecyclerViewAdapter.notifyItemRangeChanged(position, mItems.size());
            }
        }
    }

    protected void clearItems() {
        mItems.clear();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager = getLayoutManager());
        }
    }

    public int getSpanCount() {
        Activity activity;
        if ((activity = getActivity()) != null) {
            int span = Utils.isTablet(activity) ? Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 3 : 2 : Utils.getOrientation(activity) ==
                    Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
            if (itemsSize() != 0 && span > itemsSize()) {
                span = itemsSize();
            }
            return span;
        }
        return 1;
    }

    public int itemsSize() {
        return mItems.size();
    }

    protected void showProgress() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    mProgress.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    protected boolean isForeground() {
        return false;
    }

    protected BaseFragment getForegroundFragment() {
        return null;
    }

    public void setForegroundText(CharSequence text) {
        mForegroundStrText = text;
    }

    private void showViewAnimation(View view) {
        if (mSlideInOutAnimation != null) {
            mSlideInOutAnimation.cancel();
        }

        view.setVisibility(View.VISIBLE);
        mSlideInOutAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        mSlideInOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mSlideInOutAnimation.setDuration(250);
        view.startAnimation(mSlideInOutAnimation);
    }

    public void hideViewAnimation(View view) {
        if (mSlideInOutAnimation != null) {
            mSlideInOutAnimation.cancel();
        }

        mSlideInOutAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        mSlideInOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mSlideInOutAnimation.setDuration(250);
        view.startAnimation(mSlideInOutAnimation);
    }

    public void showForeground() {
        if (mForegroundStrText != null) {
            mForegroundText.setText(mForegroundStrText);
        }
        showViewAnimation(mForegroundParent);
    }

    public void dismissForeground() {
        hideViewAnimation(mForegroundParent);
    }

    protected View getRootView() {
        return mRootView;
    }

    protected Fragment getChildFragment(int position) {
        return getChildFragmentManager().getFragments().get(position);
    }

    protected int childFragmentCount() {
        return getChildFragmentManager().getFragments().size();
    }

    protected Fragment getDialogFragment() {
        return new LoadingFragment();
    }

    void showDialog(String title, String summary) {
        if (mDialogFragment instanceof LoadingFragment) {
            LoadingFragment loadingFragment = (LoadingFragment) mDialogFragment;
            loadingFragment.setTitle(title);
            loadingFragment.setSummary(summary);
        }
        showViewAnimation(mDialogParent);
    }

    void dismissDialog() {
        hideViewAnimation(mDialogParent);
    }

    protected <T extends RecyclerViewFragment> void showDialog(
            DialogLoadHandler<T> dialogLoadHandler) {
        if (mDialogLoader == null) {
            mDialogLoader = new LoadAsyncTask<>((T) this, dialogLoadHandler);
            mDialogLoader.execute();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mForegroundParent != null
                && mForegroundParent.getVisibility() == View.VISIBLE) {
            dismissForeground();
            return true;
        } else if (mDialogParent != null
                && mDialogParent.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPoolExecutor == null) {
            mPoolExecutor = new ScheduledThreadPoolExecutor(1);
            mPoolExecutor.scheduleWithFixedDelay(mScheduler, 1,
                    1, TimeUnit.SECONDS);
        }
        for (RecyclerViewItem item : mItems) {
            item.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPoolExecutor != null) {
            mPoolExecutor.shutdown();
            mPoolExecutor = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        ViewUtils.dismissDialog(getChildFragmentManager());
        super.onSaveInstanceState(outState);
    }

    protected void refreshThread() {
    }

    protected void refresh() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItems.clear();
        mRecyclerViewAdapter = null;
        if (mLoader != null) {
            mLoader.cancel(true);
            mLoader = null;
        }
        if (mReloader != null) {
            mReloader.cancel(true);
            mReloader = null;
        }
        if (mDialogLoader != null) {
            mDialogLoader.cancel(true);
            mDialogLoader = null;
        }
        for (RecyclerViewItem item : mItems) {
            item.onDestroy();
        }
    }

    protected Handler getHandler() {
        return mHandler;
    }

    private static class LoaderTask extends AsyncTask<Void, Void, List<RecyclerViewItem>> {

        private WeakReference<RecyclerViewFragment> mRefFragment;
        private Bundle mSavedInstanceState;

        private LoaderTask(RecyclerViewFragment fragment, Bundle savedInstanceState) {
            mRefFragment = new WeakReference<>(fragment);
            mSavedInstanceState = savedInstanceState;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RecyclerViewFragment fragment = mRefFragment.get();

            if (fragment != null) {
                fragment.showProgress();
                fragment.init();
            }
        }

        @Override
        protected List<RecyclerViewItem> doInBackground(Void... params) {
            RecyclerViewFragment fragment = mRefFragment.get();

            if (fragment != null && fragment.isAdded()
                    && fragment.getActivity() != null) {
                List<RecyclerViewItem> items = new ArrayList<>();
                fragment.addItems(items);
                return items;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecyclerViewItem> recyclerViewItems) {
            super.onPostExecute(recyclerViewItems);
            RecyclerViewFragment fragment = mRefFragment.get();

            if (isCancelled() || recyclerViewItems == null || fragment == null) return;

            for (RecyclerViewItem item : recyclerViewItems) {
                fragment.addItem(item);
            }
            fragment.hideProgress();
            fragment.postInit();
            if (mSavedInstanceState == null) {
                fragment.mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = fragment.getActivity();
                        if (fragment.isAdded() && activity != null) {
                            fragment.mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    activity, R.anim.slide_in_bottom));
                        }
                    }
                });
            }
            fragment.mLoader = null;
        }
    }

    public static class ReloadHandler<T extends RecyclerViewFragment>
            extends LoadAsyncTask.LoadHandler<T, List<RecyclerViewItem>> {

        @Override
        public void onPreExecute(T fragment) {
            super.onPreExecute(fragment);

            fragment.showProgress();
        }

        @Override
        public List<RecyclerViewItem> doInBackground(T fragment) {
            List<RecyclerViewItem> items = new ArrayList<>();
            fragment.load(items);
            return items;
        }

        @Override
        public void onPostExecute(T fragment,
                                  List<RecyclerViewItem> items) {
            super.onPostExecute(fragment, items);

            for (RecyclerViewItem item : items) {
                fragment.addItem(item);
            }
            fragment.hideProgress();
            fragment.mReloader = null;
        }
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments;

        public ViewPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments == null ? 0 : mFragments.size();
        }
    }

    public abstract static class DialogLoadHandler<T extends RecyclerViewFragment>
            extends LoadAsyncTask.LoadHandler<T, Void> {
        private String mTitle;
        private String mSummary;

        public DialogLoadHandler(String title, String summary) {
            mTitle = title;
            mSummary = summary;
        }

        @Override
        public void onPreExecute(T fragment) {
            super.onPreExecute(fragment);

            fragment.showDialog(mTitle, mSummary);
        }

        @Override
        public void onPostExecute(T fragment, Void aVoid) {
            super.onPostExecute(fragment, aVoid);

            fragment.dismissDialog();
            fragment.mDialogLoader = null;
        }
    }
}
