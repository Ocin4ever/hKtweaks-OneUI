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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.hades.hKtweaks.R;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.menu.MenuItem;

/**
 * Created by willi on 01.07.16.
 */
public class EditorActivity extends BaseActivity {

    public static final String TITLE_INTENT = "title";
    public static final String TEXT_INTENT = "text";
    private static final String EDITTEXT_INTENT = "edittext";

    private AppCompatEditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        String title = getIntent().getStringExtra(TITLE_INTENT);

        ToolbarLayout toolbarLayout;
        toolbarLayout = getToolBarLayout();
        toolbarLayout.setTitle(title != null ? title : "");
        toolbarLayout.setNavigationButtonOnClickListener(v -> onBackPressed());

        toolbarLayout.inflateToolbarMenu(R.menu.save_menu);
        toolbarLayout.setOnToolbarMenuItemClickListener(item -> {
            Intent intent = new Intent();
            intent.putExtra(TEXT_INTENT, mEditText.getText());
            setResult(0, intent);
            finish();
            return true;
        });

        CharSequence text = getIntent().getCharSequenceExtra(TEXT_INTENT);
        mEditText = findViewById(R.id.edittext);
        if (text != null) {
            mEditText.append(text);
        }
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineCounter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditText.post(() -> updateLineCounter(mEditText.getText().toString()));
    }

    private void updateLineCounter(String text) {
        StringBuilder lineCounterText = new StringBuilder();
        String[] lines = new String[mEditText.getLineCount()];

        int lineBreakCount = 0, start = 0, end;

        for (int i = 0; i < mEditText.getLineCount(); i++) {
            end = mEditText.getLayout().getLineEnd(i);
            lines[i] = text.substring(start, end);

            if (i == 0 || lines[i - 1].contains("\n")) {
                lineBreakCount++;
                lineCounterText.append(lineBreakCount).append("\n");
            } else lineCounterText.append("\n");

            start = end;
        }
        ((TextView) findViewById(R.id.editor_lines)).setText(lineCounterText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(EDITTEXT_INTENT, mEditText.getText());
    }

}
