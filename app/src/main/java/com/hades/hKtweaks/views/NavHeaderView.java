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
package com.hades.hKtweaks.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hades.hKtweaks.R;
import com.hades.hKtweaks.utils.AppSettings;
import com.hades.hKtweaks.utils.Utils;
import com.hades.hKtweaks.utils.ViewUtils;
import com.hades.hKtweaks.views.dialog.Dialog;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by willi on 28.12.15.
 */
public class NavHeaderView extends LinearLayout {

    private static Callback sCallback;
    private ImageView mImage;

    public NavHeaderView(Context context) {
        this(context, null);
    }

    public NavHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavHeaderView(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sCallback = new Callback() {
            @Override
            public void setImage(Uri uri) throws IOException {
                NavHeaderView.this.setImage(uri);
            }
        };

        LayoutInflater.from(context).inflate(R.layout.nav_header_view, this);
        mImage = findViewById(R.id.nav_header_pic);

        try {
            if (AppSettings.isPreviewPictureEmpty(context)) {
                String uri = AppSettings.getPreviewPicture(getContext());
                if (uri == null) {
                    mImage.setImageDrawable(null);
                } else {
                    setImage(Uri.parse(uri));
                }
            } else {
                mImage.setImageResource(R.drawable.hades_adaptive_logo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        findViewById(R.id.nav_header_fab).setOnClickListener(v
                -> new Dialog(context).setItems(v.getResources()
                        .getStringArray(R.array.main_header_picture_items),
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            v.getContext().startActivity(new Intent(v.getContext(),
                                    MainHeaderActivity.class));
                            break;
                        case 1:
                            AppSettings.resetPreviewPicture(getContext());
                            AppSettings.setPreviewPictureEmpty(true, context);
                            mImage.setImageDrawable(null);
                            break;
                    }
                }).show());
    }

    private static Bitmap uriToBitmap(Uri uri, Context context) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        }
        throw new IOException();
    }

    public void setImage(Uri uri) throws IOException, NullPointerException {
        String selectedImagePath = null;
        try {
            selectedImagePath = Utils.getPath(uri, mImage.getContext());
        } catch (Exception ignored) {
        }
        Bitmap bitmap;
        if ((bitmap = selectedImagePath != null ? BitmapFactory.decodeFile(selectedImagePath) :
                uriToBitmap(uri, mImage.getContext())) != null) {
            mImage.setImageBitmap(ViewUtils.scaleDownBitmap(bitmap, 1024, 1024));
        } else {
            throw new NullPointerException("Getting Bitmap failed");
        }
    }

    private interface Callback {
        void setImage(Uri uri) throws IOException;
    }

    public static class MainHeaderActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            } else {
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), 0);
        }

        @Override
        protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK && requestCode == 0)
                try {
                    Uri selectedImageUri = data.getData();
                    sCallback.setImage(selectedImageUri);
                    AppSettings.savePreviewPicture(selectedImageUri.toString(), this);
                    AppSettings.setPreviewPictureEmpty(true, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.toast(R.string.went_wrong, MainHeaderActivity.this);
                }
            finish();
        }
    }

}
