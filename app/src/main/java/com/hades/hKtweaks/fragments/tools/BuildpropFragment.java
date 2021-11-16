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
package com.hades.hKtweaks.fragments.tools;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.BaseActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.root.RootUtils;
import com.hades.hKtweaks.utils.tools.Buildprop;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;

import java.util.LinkedHashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

/**
 * Created by willi on 10.07.16.
 */
public class BuildpropFragment extends RecyclerViewFragment {

    private LinkedHashMap<String, String> mProps;
    private String mSearchText;

    private Dialog mAddDialog;
    private Dialog mItemOptionsDialog;
    private Dialog mDeleteDialog;

    private String mKey;
    private String mValue;

    private ToolbarLayout toolbarLayout;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbarLayout = ((BaseActivity) getActivity()).getToolBarLayout();
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> toolbarLayout.onSearchModeVoiceInputResult(result));
    }

    @Override
    protected void init() {
        super.init();
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setBackgroundResource(R.color.item_background_color);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.seslSetFastScrollerEnabled(true);
        recyclerView.seslSetGoToTopEnabled(true);

        showToolbarActionButton(item -> {
            switch (item.getItemId()) {
                case R.id.menu_search:
                    toolbarLayout.showSearchMode();
                    toolbarLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
                        @Override
                        public void onSearchOpened(EditText search_edittext) {
                        }

                        @Override
                        public void onSearchDismissed(EditText search_edittext) {
                            search_edittext.setText(null);
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            mSearchText = s.toString();
                            reload(false);
                        }

                        @Override
                        public void onKeyboardSearchClick(CharSequence s) {
                        }

                        @Override
                        public void onVoiceInputClick(Intent intent) {
                            activityResultLauncher.launch(intent);
                        }
                    });
                    break;
                case R.id.menu_add:
                    mAddDialog = new Dialog(getActivity()).setItems(getResources().getStringArray(
                            R.array.build_prop_add_options),
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        modify(null, null);
                                        break;
                                    case 1:
                                        Buildprop.backup();
                                        Utils.toast(getString(R.string.backup_item, Buildprop.BUILD_PROP,
                                                Utils.getInternalDataStorage()), getActivity(), Toast.LENGTH_LONG);
                                        break;
                                }
                            }).setOnDismissListener(dialog -> mAddDialog = null);
                    mAddDialog.show();
                    break;
            }
        }, R.id.menu_search, R.id.menu_add);

        if (mAddDialog != null) {
            mAddDialog.show();
        }
        if (mItemOptionsDialog != null) {
            mItemOptionsDialog.show();
        }
        if (mDeleteDialog != null) {
            mDeleteDialog.show();
        }
        if (mKey != null) {
            modify(mKey, mValue);
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        mProps = Buildprop.getProps();
        load(items);
    }

    private void reload(boolean read) {
        getHandler().postDelayed(() -> {
            clearItems();
            reload(new ReloadHandler(read));
        }, 250);
    }

    @Override
    protected void load(List<RecyclerViewItem> items) {
        super.load(items);

        if (mProps == null) return;
        String[] titles = mProps.keySet().toArray(new String[mProps.size()]);
        for (int i = 0; i < mProps.size(); i++) {
            final String title = titles[i];
            final String value = mProps.values().toArray(new String[mProps.size()])[i];
            if (mSearchText != null && !title.contains(mSearchText) && !value.contains(mSearchText)) {
                continue;
            }

            int color = ViewUtils.getThemeAccentColor(getActivity());
            String colorCode = "#"
                    + Integer.toHexString(Color.red(color))
                    + Integer.toHexString(Color.green(color))
                    + Integer.toHexString(Color.blue(color));

            DescriptionView descriptionView = new DescriptionView();
            if (mSearchText != null && !mSearchText.isEmpty()) {
                descriptionView.setTitle(Utils.htmlFrom(title.replace(mSearchText, "<b><font color=\"" + colorCode + "\">" + mSearchText + "</font></b>")));
                descriptionView.setSummary(Utils.htmlFrom(value.replace(mSearchText, "<b><font color=\"" + colorCode + "\">" + mSearchText + "</font></b>")));
            } else {
                descriptionView.setTitle(title);
                descriptionView.setSummary(value);
            }

            descriptionView.setOnItemClickListener(item -> {
                mItemOptionsDialog = new Dialog(getActivity()).setItems(
                        getResources().getStringArray(R.array.build_prop_item_options),
                        (dialogInterface, i1) -> {
                            switch (i1) {
                                case 0:
                                    modify(title, value);
                                    break;
                                case 1:
                                    delete(title, value);
                                    break;
                            }
                        })
                        .setOnDismissListener(dialogInterface -> mItemOptionsDialog = null);
                mItemOptionsDialog.show();
            });

            items.add(descriptionView);
        }
    }

    private void modify(final String key, final String value) {
        mKey = key;
        mValue = value;
        ViewUtils.dialogEditTexts(key, value, getString(R.string.key), getString(R.string.value),
                (dialogInterface, i) -> {
                },
                (text, text2) -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.key_empty, getActivity());
                        return;
                    }

                    if (key != null) {
                        overwrite(key.trim(), value.trim(), text.trim(), text2.trim());
                    } else {
                        add(text.trim(), text2.trim());
                    }
                }, getActivity())
                .setOnDismissListener(dialogInterface -> {
                    mKey = null;
                    mValue = null;
                }).show();
    }

    private void delete(final String key, final String value) {
        mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                (dialogInterface, i) -> {
                },
                (dialogInterface, i)
                        -> overwrite(key.trim(), value.trim(), "#" + key.trim(), value.trim()),
                dialogInterface -> mDeleteDialog = null, getActivity())
                .setTitle(key);
        mDeleteDialog.show();
    }

    private void add(String key, String value) {
        Buildprop.addKey(key, value);
        reload(true);
    }

    private void overwrite(String oldKey, String oldValue, String newKey, String newValue) {
        Buildprop.overwrite(oldKey, oldValue, newKey, newValue);
        reload(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RootUtils.mount(false, "/system");
        mSearchText = null;
    }

    private static class ReloadHandler extends RecyclerViewFragment.ReloadHandler<BuildpropFragment> {
        private boolean mRead;

        private ReloadHandler(boolean read) {
            mRead = read;
        }

        @Override
        public List<RecyclerViewItem> doInBackground(BuildpropFragment fragment) {
            if (mRead) {
                fragment.mProps = Buildprop.getProps();
            }
            return super.doInBackground(fragment);
        }
    }

}
