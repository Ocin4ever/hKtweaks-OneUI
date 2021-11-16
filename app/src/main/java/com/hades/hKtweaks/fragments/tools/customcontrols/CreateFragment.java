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
package com.hades.hKtweaks.fragments.tools.customcontrols;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.activities.EditorActivity;
import com.hades.hKtweaks.activities.tools.CustomControlsActivity;
import com.hades.hKtweaks.fragments.recyclerview.RecyclerViewFragment;
import com.hades.hKtweaks.utils.tools.customcontrols.Items;
import com.hades.hKtweaks.views.recyclerview.EditTextView;
import com.hades.hKtweaks.views.recyclerview.RecyclerViewItem;
import com.hades.hKtweaks.views.recyclerview.customcontrols.CodeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by willi on 01.07.16.
 */
public class CreateFragment extends RecyclerViewFragment {

    private static final String SETTINGS_INTENT = "settings";
    private ArrayList<Items.Setting> mSettings;
    private HashMap<Items.Setting, EditTextView> mEditTextViews = new HashMap<>();
    private HashMap<Items.Setting, CodeView> mCodeViews = new HashMap<>();

    public static CreateFragment newInstance(ArrayList<Items.Setting> settings) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(SETTINGS_INTENT, settings);
        CreateFragment fragment = new CreateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void init() {
        super.init();
        getRecyclerView().setBackgroundResource(R.color.item_background_color);

        mSettings = getArguments().getParcelableArrayList(SETTINGS_INTENT);
        showFab();
    }

    @Override
    protected void addItems(List<RecyclerViewItem> items) {
        mEditTextViews.clear();
        mCodeViews.clear();
        for (final Items.Setting setting : mSettings) {
            if (setting.isScript()) {
                final CodeView codeView = new CodeView();
                codeView.setTitle(setting.getName(getActivity()));
                codeView.setSummary(setting.getDescription(getActivity()));
                codeView.setRequired(setting.isRequired());
                codeView.setCode(setting.getDefault());
                codeView.setTesting(setting.getUnit() != Items.Setting.Unit.APPLY);

                codeView.setOnItemClickListener(item -> {
                    Intent intent = new Intent(getActivity(), EditorActivity.class);
                    intent.putExtra(EditorActivity.TITLE_INTENT, setting.getName(getActivity()).toString());
                    intent.putExtra(EditorActivity.TEXT_INTENT, codeView.getCode());
                    startActivityForResult(intent, mSettings.indexOf(setting));
                });
                codeView.setOnTestListener((codeView1, output) -> showFab());

                items.add(codeView);
                mCodeViews.put(setting, codeView);
            } else if (setting.getUnit() != Items.Setting.Unit.ID) {
                EditTextView editTextView = new EditTextView();
                editTextView.setTitle(setting.getName(getActivity()));
                if (setting.isRequired()) {
                    editTextView.setHint(getString(R.string.required));
                }
                if (setting.getUnit() == Items.Setting.Unit.INTEGER) {
                    editTextView.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
                editTextView.setText(setting.getDefault());
                editTextView.setTextWatcher(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        showFab();
                    }
                });

                items.add(editTextView);
                mEditTextViews.put(setting, editTextView);
            }
        }
    }

    private void showFab() {
        boolean show = mEditTextViews.size() > 0 && mCodeViews.size() > 0;
        for (Items.Setting setting : mEditTextViews.keySet()) {
            if (setting.isRequired() && mEditTextViews.get(setting).getText() != null
                    && mEditTextViews.get(setting).getText().toString().isEmpty()) {
                show = false;
                break;
            }
        }
        for (Items.Setting setting : mCodeViews.keySet()) {
            if (setting.isRequired() &&
                    !patternMatches(setting.getUnit(), mCodeViews.get(setting).getOutput())) {
                show = false;
                break;
            }
        }
        if (show) {
            showToolbarActionButton(item -> {
                HashMap<String, Object> arguments = new HashMap<>();
                for (Items.Setting setting : mSettings) {
                    if (setting.getUnit() == Items.Setting.Unit.ID) {
                        arguments.put("id", setting.getName(getActivity()).toString());
                        arguments.put("uniqueId", setting.getUniqueId());
                    } else if (mEditTextViews.containsKey(setting)) {
                        String text = mEditTextViews.get(setting).getText().toString();
                        if (!text.isEmpty()) {
                            arguments.put(setting.getId(), text);
                        }
                    } else if (mCodeViews.containsKey(setting)) {
                        arguments.put(setting.getId(), mCodeViews.get(setting).getCode().toString());
                    }
                }

                Intent intent = new Intent();
                intent.putExtra(CustomControlsActivity.RESULT_INTENT, arguments);
                getActivity().setResult(0, intent);
                getActivity().finish();
            }, R.id.menu_done);
        } else {
            hideToolbarActionButton();
        }
    }

    private boolean patternMatches(Items.Setting.Unit unit, String output) {
        if (unit == Items.Setting.Unit.APPLY) return true;
        if (output != null && !output.isEmpty()) {
            switch (unit) {
                case BOOLEAN:
                    return output.matches("([01])");
                case INTEGER:
                    return output.matches("(|-)\\d*");
                case STRING:
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0 && data != null) {
            CharSequence text = data.getCharSequenceExtra(EditorActivity.TEXT_INTENT);
            if (text != null) {
                mCodeViews.get(mSettings.get(requestCode)).resetTest();
                mCodeViews.get(mSettings.get(requestCode)).setCode(text.toString().trim());
                showFab();
            }
        }
    }

}
