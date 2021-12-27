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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.EditorActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.utils.root.RootUtils;
import com.hades.hKtweaks.utils.tools.Initd;
import com.hades.hKtweaks.views.dialog.Dialog;
import com.hades.hKtweaks.views.recyclerview.CardView;
import com.hades.hKtweaks.views.recyclerview.DescriptionView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.SwitcherFView;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.menu.Menu;
import de.dlyt.yanndroid.oneui.menu.MenuItem;
import de.dlyt.yanndroid.oneui.menu.PopupMenu;

/**
 * Created by willi on 16.07.16.
 */
public class InitdFragment extends RecyclerViewFragment {

    private Dialog mExecuteDialog;
    private Dialog mResultDialog;
    private Dialog mDeleteDialog;
    private boolean mShowCreateNameDialog;

    private String mCreateName;

    private String mEditInitd;

    @Override
    protected void init() {
        super.init();
        showToolbarActionButton(item -> {
            showCreateDialog();
            return true;
        }, R.id.menu_add);

        if (mExecuteDialog != null) {
            mExecuteDialog.show();
        }
        if (mResultDialog != null) {
            mResultDialog.show();
        }
        if (mDeleteDialog != null) {
            mDeleteDialog.show();
        }
        if (mShowCreateNameDialog) {
            showCreateDialog();
        }
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        load(items);
    }

    private void reload() {
        getHandler().postDelayed(() -> {
            clearItems();
            reload(new ReloadHandler<>());
        }, 250);
    }

    @Override
    protected void load(List<RecyclerViewItem> items) {
        super.load(items);

        items.add(new SwitcherFView(
                getString(R.string.emulate_initd),
                getString(R.string.emulate_initd_summary),
                AppSettings.isInitdOnBoot(getActivity()),
                (compoundButton, b) -> AppSettings.saveInitdOnBoot(b, getActivity())));

        for (final String initd : Initd.list()) {
            CardView cardView = new CardView(getActivity());
            cardView.setOnMenuListener((cardView1, popupMenu) -> {

                Menu menu = new Menu();
                menu.addMenuItem(new MenuItem(0, getString(R.string.edit), null));
                menu.addMenuItem(new MenuItem(1, getString(R.string.delete), null));

                popupMenu.inflate(menu);
                popupMenu.setPopupMenuListener(new PopupMenu.PopupMenuListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case 0:
                                mEditInitd = initd;
                                Intent intent = new Intent(getActivity(), EditorActivity.class);
                                intent.putExtra(EditorActivity.TITLE_INTENT, initd);
                                intent.putExtra(EditorActivity.TEXT_INTENT, Initd.read(initd));
                                startActivityForResult(intent, 0);
                                break;
                            case 1:
                                mDeleteDialog = ViewUtils.dialogBuilder(getString(R.string.sure_question),
                                        (dialogInterface, i) -> {
                                        },
                                        (dialogInterface, i) -> {
                                            Initd.delete(initd);
                                            reload();
                                        },
                                        dialogInterface -> mDeleteDialog = null, getActivity());
                                mDeleteDialog.show();
                                break;
                        }
                        return true;
                    }

                    @Override
                    public void onMenuItemUpdate(MenuItem menuItem) {

                    }
                });
            });

            DescriptionView descriptionView = new DescriptionView();
            descriptionView.setSummary(initd);
            descriptionView.setOnItemClickListener(item -> {
                mExecuteDialog = ViewUtils.dialogBuilder(getString(R.string.exceute_question, initd),
                        (dialogInterface, i) -> {
                        },
                        (dialogInterface, i) -> execute(initd),
                        dialogInterface -> mExecuteDialog = null,
                        getActivity());
                mExecuteDialog.show();
            });

            cardView.addItem(descriptionView);
            items.add(cardView);
        }
    }

    private void execute(final String initd) {
        showDialog(new ExecuteTask(getActivity(), initd));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;
        if (requestCode == 0) {
            Initd.write(mEditInitd, data.getCharSequenceExtra(EditorActivity.TEXT_INTENT).toString());
            reload();
        } else if (requestCode == 1) {
            Initd.write(mCreateName, data.getCharSequenceExtra(EditorActivity.TEXT_INTENT).toString());
            mCreateName = null;
            reload();
        }
    }

    private void showCreateDialog() {
        mShowCreateNameDialog = true;
        ViewUtils.dialogEditText(null,
                (dialogInterface, i) -> {
                },
                text -> {
                    if (text.isEmpty()) {
                        Utils.toast(R.string.name_empty, getActivity());
                        return;
                    }

                    if (Initd.list().indexOf(text) > -1) {
                        Utils.toast(getString(R.string.already_exists, text), getActivity());
                        return;
                    }

                    mCreateName = text;
                    Intent intent = new Intent(getActivity(), EditorActivity.class);
                    intent.putExtra(EditorActivity.TITLE_INTENT, mCreateName);
                    intent.putExtra(EditorActivity.TEXT_INTENT, "#!/system/bin/sh\n\n");
                    startActivityForResult(intent, 1);
                }, getActivity()).setTitle(getString(R.string.name))
                .setOnDismissListener(
                        dialogInterface -> mShowCreateNameDialog = false).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RootUtils.mount(false, "/system");
    }

    private static class ExecuteTask extends DialogLoadHandler<InitdFragment> {
        private String mInitd;
        private String mResult;

        private ExecuteTask(Context context, String initd) {
            super(null, context.getString(R.string.executing));
            mInitd = initd;
        }

        @Override
        public Void doInBackground(InitdFragment fragment) {
            mResult = Initd.execute(mInitd);
            return null;
        }

        @Override
        public void onPostExecute(InitdFragment fragment, Void aVoid) {
            super.onPostExecute(fragment, aVoid);

            if (mResult != null && !mResult.isEmpty()) {
                fragment.mResultDialog = ViewUtils.dialogBuilder(mResult,
                        null,
                        null,
                        dialogInterface -> fragment.mResultDialog = null,
                        fragment.getActivity()).setTitle(
                        fragment.getString(R.string.result));
                fragment.mResultDialog.show();
            }
        }
    }
}
