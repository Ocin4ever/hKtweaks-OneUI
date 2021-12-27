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
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.root.RootFile;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;

/**
 * Created by willi on 04.07.16.
 */
public class FilePickerActivity extends BaseActivity {

    public static final String PATH_INTENT = "path";
    public static final String EXTENSION_INTENT = "extension";
    public static final String RESULT_INTENT = "result";

    private String mPath;
    private String mExtension;
    private FilePickerFragment mFragment;

    private ToolbarLayout toolbarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);

        toolbarLayout = super.getToolBarLayout();
        toolbarLayout.setNavigationButtonOnClickListener(v -> super.onBackPressed());

        mPath = getIntent().getStringExtra(PATH_INTENT);
        mExtension = getIntent().getStringExtra(EXTENSION_INTENT);

        RootFile path = new RootFile(mPath);
        if (!path.exists() || !path.isDirectory()) {
            mPath = "/";
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragment
                = (FilePickerFragment) getFragment(), "fragment").commit();
    }

    @Override
    public ToolbarLayout getToolBarLayout() {
        return toolbarLayout;
    }

    private Fragment getFragment() {
        Fragment filePickerFragment = getSupportFragmentManager().findFragmentByTag("fragment");
        if (filePickerFragment == null) {
            filePickerFragment = FilePickerFragment.newInstance(mPath, mExtension);
        }
        return filePickerFragment;
    }

    @Override
    public void onBackPressed() {
        if (mFragment != null && !mFragment.mPath.equals("/")) {
            mFragment.mPath = new RootFile(mFragment.mPath).getParentFile().toString();
            mFragment.reload();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        getSupportFragmentManager().beginTransaction().remove(getFragment()).commit();
        super.finish();
    }

    public static class FilePickerFragment extends RecyclerViewFragment {

        private String mPath;
        private String mExtension;
        private Drawable mDirImage;
        private Drawable[] mFileImages;
        private Dialog mPickDialog;

        public static FilePickerFragment newInstance(String path, String extension) {
            Bundle args = new Bundle();
            args.putString(PATH_INTENT, path);
            args.putString(EXTENSION_INTENT, extension);
            FilePickerFragment fragment = new FilePickerFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        protected void init() {
            super.init();
            if (mPath == null) {
                mPath = getArguments().getString(PATH_INTENT);
            }
            if (mExtension == null) {
                mExtension = getArguments().getString(EXTENSION_INTENT);
            }
            if (mDirImage == null) {
                mDirImage = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_folder));
            }
            if (mFileImages == null) {
                mFileImages = new Drawable[]{
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_apk)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_audio)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_etc)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_image)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_txt)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_video)),
                        DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_samsung_file_type_zip))
                };
            }
            if (mPickDialog != null) {
                mPickDialog.show();
            }

            ToolbarLayout toolbarLayout;
            if ((toolbarLayout = ((FilePickerActivity) getActivity()).getToolBarLayout()) != null) {
                toolbarLayout.setTitle(mPath);
            }


        }

        @Override
        protected void addItems(List<RecyclerViewItem> items) {
            load(items);
        }

        @Override
        protected void postInit() {
            super.postInit();
            ToolbarLayout toolbarLayout;
            if ((toolbarLayout = ((FilePickerActivity) getActivity()).getToolBarLayout()) != null) {
                toolbarLayout.setTitle(mPath);
            }
        }

        private void reload() {
            clearItems();
            reload(new ReloadHandler());
        }

        @Override
        protected void load(List<RecyclerViewItem> items) {
            super.load(items);

            RootFile path = new RootFile(mPath).getRealPath();
            mPath = path.toString();

            if (!path.isDirectory()) {
                mPath = path.getParentFile().toString();
                reload();
                return;
            }
            List<RootFile> dirs = new ArrayList<>();
            List<RootFile> files = new ArrayList<>();
            for (RootFile file : path.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file);
                } else {
                    files.add(file);
                }
            }

            final RootFile returnDir = path.getParentFile();
            if (returnDir.isDirectory()) {
                DescriptionView descriptionViewParent = new DescriptionView();
                descriptionViewParent.setTitle("..");
                descriptionViewParent.setDrawable(mDirImage);
                descriptionViewParent.setOnItemClickListener(item -> {
                    mPath = returnDir.toString();
                    reload();
                });

                items.add(descriptionViewParent);
            }

            for (final RootFile dir : dirs) {
                DescriptionView descriptionView = new DescriptionView();
                descriptionView.setTitle(dir.getName());
                int itemCount = dir.list().size();
                descriptionView.setSummary(itemCount == 1 ? getString(R.string.one_item) : getString(R.string.item_count, itemCount));
                descriptionView.setDrawable(mDirImage);
                descriptionView.setOnItemClickListener(item -> {
                    mPath = dir.toString();
                    reload();
                });

                items.add(descriptionView);
            }
            for (final RootFile file : files) {
                DescriptionView descriptionView = new DescriptionView();
                descriptionView.setTitle(file.getName());
                descriptionView.setDrawable(getFileDrawable(file));
                descriptionView.setOnItemClickListener(item -> {
                    if (mExtension != null && !mExtension.isEmpty() && file.getName() != null
                            && !file.getName().endsWith(mExtension)) {
                        Utils.toast(getString(R.string.wrong_extension, mExtension), getActivity());
                    } else {
                        mPickDialog =
                                ViewUtils.dialogBuilder(getString(R.string.select_question,
                                        file.getName()),
                                        (dialog, which) -> {
                                        },
                                        (dialog, which) -> {
                                            Intent intent = new Intent();
                                            intent.putExtra(RESULT_INTENT, file.toString());
                                            getActivity().setResult(0, intent);
                                            getActivity().finish();
                                        },
                                        dialog -> mPickDialog = null, getActivity());
                        mPickDialog.show();
                    }
                });

                items.add(descriptionView);
            }
        }

        private static class ReloadHandler
                extends RecyclerViewFragment.ReloadHandler<FilePickerFragment> {
            @Override
            public void onPostExecute(FilePickerFragment fragment, List<RecyclerViewItem> items) {
                super.onPostExecute(fragment, items);

                FilePickerActivity activity = (FilePickerActivity) fragment.getActivity();
                ToolbarLayout toolbarLayout;
                if (activity != null
                        && (toolbarLayout = activity.getToolBarLayout()) != null) {
                    toolbarLayout.setTitle(fragment.mPath);
                }
            }
        }

        private Drawable getFileDrawable(RootFile file) {
            String fileName = file.getName();

            if (fileName.lastIndexOf(".") == -1) return mFileImages[2];
            switch (fileName.substring(fileName.lastIndexOf("."), fileName.length())) {
                case ".apk":
                    return mFileImages[0];
                case ".mp3":
                case ".wav":
                case ".flac":
                case ".m3u":
                    return mFileImages[1];
                case ".png":
                case ".jpg":
                case ".jpeg":
                case ".gif":
                    return mFileImages[3];
                case ".txt":
                case ".json":
                case ".html":
                case ".pdf":
                    return mFileImages[4];
                case ".mp4":
                case ".mov":
                case ".wmv":
                case ".avi":
                case ".mkv":
                    return mFileImages[5];
                case ".zip":
                case ".rar":
                case ".7z":
                    return mFileImages[6];

                default:
                    return mFileImages[2];
            }
        }

    }

}
