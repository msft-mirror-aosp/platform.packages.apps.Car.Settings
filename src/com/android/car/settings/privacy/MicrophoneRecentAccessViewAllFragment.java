/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.car.settings.privacy;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.android.car.settings.R;
import com.android.car.settings.common.SettingsFragment;
import com.android.car.ui.toolbar.MenuItem;

import java.util.Arrays;
import java.util.List;

/**
 * All apps that have recently accessed microphone.
 */
public class MicrophoneRecentAccessViewAllFragment extends SettingsFragment {

    private boolean mShowSystem = false;
    private MenuItem mShowHideSystemMenu;
    private MicrophoneRecentAccessViewAllPreferenceController mController;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.microphone_recent_requests_view_all_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowHideSystemMenu = new MenuItem.Builder(getContext())
                .setTitle(R.string.show_system)
                .setOnClickListener(i -> setShowSystem(!mShowSystem))
                .build();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mController = use(MicrophoneRecentAccessViewAllPreferenceController.class,
                R.string.pk_microphone_recent_requests);
    }

    @Override
    protected List<MenuItem> getToolbarMenuItems() {
        return Arrays.asList(mShowHideSystemMenu);
    }

    private void setShowSystem(boolean showSystem) {
        if (showSystem != mShowSystem) {
            mShowSystem = showSystem;
            mController.setShowSystem(showSystem);
            updateMenu();
        }
    }

    private void updateMenu() {
        mShowHideSystemMenu.setTitle(mShowSystem ? R.string.hide_system : R.string.show_system);
    }
}
