/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.settings.wifi.details;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.android.car.settings.R;
import com.android.car.settings.common.BaseFragment;
import com.android.car.settings.common.Logger;
import com.android.settingslib.qrcode.QrCodeGenerator;

/**
 * Shows the easy connect QR code for a Wi-Fi network.
 */
public class WifiDetailsShareFragment extends BaseFragment {
    private static final Logger LOG = new Logger(WifiDetailsShareFragment.class);
    private static final String WIFI_SHARE_KEY = "wifi_share_uri";

    /**
     * Gets an instance of this class.
     */
    public static WifiDetailsShareFragment getInstance(String uri) {
        WifiDetailsShareFragment wifiDetailsShareFragment = new WifiDetailsShareFragment();
        Bundle bundle = new Bundle();
        bundle.putString(WIFI_SHARE_KEY, uri);
        wifiDetailsShareFragment.setArguments(bundle);
        return wifiDetailsShareFragment;
    }

    @Override
    @LayoutRes
    protected int getLayoutId() {
        return R.layout.wifi_detail_share_fragment;
    }

    @Override
    @StringRes
    protected int getTitleId() {
        return R.string.wifi_detail_share;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String uri = getArguments().getString(WIFI_SHARE_KEY);
        ImageView qr_image = view.findViewById(R.id.wifi_share_qr_code);
        try {
            int size = getContext().getResources().getDimensionPixelSize(
                    R.dimen.hotspot_qr_code_size);
            int margin = getContext().getResources().getDimensionPixelSize(
                    R.dimen.qr_code_margin);
            Bitmap bmp = QrCodeGenerator.encodeQrCode(uri, size, margin);
            qr_image.setImageBitmap(bmp);
        } catch (Exception e) {
            LOG.w("Failed to load wifi easy share qr code: " + e);
        }
    }
}
