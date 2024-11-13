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

package com.android.car.settings.applications.appinfo;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceGroup;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.Logger;
import com.android.car.settings.common.PreferenceController;
import com.android.car.settings.common.RadioWithImagePreference;
import com.android.internal.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

/**
 * A PreferenceController handling the logic for selecting an aspect ratio.
 */
public class AppAspectRatiosGroupPreferenceController extends
        PreferenceController<PreferenceGroup> implements RadioWithImagePreference.OnClickListener {
    private static final Logger LOG = new Logger(AppAspectRatiosGroupPreferenceController.class);
    private static final String KEY_PREF_DEFAULT = "app_default_pref";
    private static final String KEY_PREF_FULLSCREEN = "fullscreen_pref";
    private static final String KEY_PREF_HALF_SCREEN = "half_screen_pref";
    private static final String KEY_PREF_DISPLAY_SIZE = "display_size_pref";
    private static final String KEY_PREF_16_9 = "16_9_pref";
    private static final String KEY_PREF_4_3 = "4_3_pref";
    private static final String KEY_PREF_3_2 = "3_2_pref";
    private List<RadioWithImagePreference> mPreferenceList;
    private String mSelectedKey = KEY_PREF_DEFAULT;
    private String mPackageName;
    private int mUserId;
    private AspectRatioManager mAspectRatioManager;

    public AppAspectRatiosGroupPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions restrictionInfo) {
        super(context, preferenceKey, fragmentController, restrictionInfo);
        mPreferenceList = new ArrayList<>();
        mUserId = UserHandle.myUserId();
        mAspectRatioManager = new AspectRatioManager(context);
    }

    /**
     * Set the packageName, which is used to perform actions on a particular package.
     */
    public AppAspectRatiosGroupPreferenceController setPackageName(String packageName) {
        mPackageName = packageName;
        return this;
    }

    @Override
    protected Class<PreferenceGroup> getPreferenceType() {
        return PreferenceGroup.class;
    }

    @Override
    protected void onCreateInternal() {
        for (int i = 0; i < getPreference().getPreferenceCount(); i++) {
            // TODO(b/375471891): Add more conditions when setting up.
            RadioWithImagePreference child =
                    (RadioWithImagePreference) getPreference().getPreference(i);
            mPreferenceList.add(child);
            child.setOnClickListener(this);
        }
    }

    @Override
    protected void updateState(PreferenceGroup preference) {
        super.updateState(preference);
        for (RadioWithImagePreference child : mPreferenceList) {
            child.setChecked(child.getKey().equals(mSelectedKey));
        }
    }

    @Override
    public void onRadioButtonClicked(RadioWithImagePreference selected) {
        String selectedKey = selected.getKey();
        if (selectedKey.equals(mSelectedKey)) {
            return;
        }
        int userAspectRatio = getSelectedAspectRatioAction(selectedKey);

        try {
            getAspectRatioManager().setUserMinAspectRatio(mPackageName, mUserId, userAspectRatio);
            mSelectedKey = selectedKey;
        } catch (RemoteException e) {
            LOG.e("Unable to set user min aspect ratio");
            return;
        }
        updateState(getPreference());
    }

    private static int getSelectedAspectRatioAction(@NonNull String selectedKey) {
        switch (selectedKey) {
            case KEY_PREF_DEFAULT:
                return PackageManager.USER_MIN_ASPECT_RATIO_APP_DEFAULT;
            case KEY_PREF_FULLSCREEN:
                return PackageManager.USER_MIN_ASPECT_RATIO_FULLSCREEN;
            case KEY_PREF_HALF_SCREEN:
                return PackageManager.USER_MIN_ASPECT_RATIO_SPLIT_SCREEN;
            case KEY_PREF_4_3:
                return PackageManager.USER_MIN_ASPECT_RATIO_4_3;
            case KEY_PREF_16_9:
                return PackageManager.USER_MIN_ASPECT_RATIO_16_9;
            case KEY_PREF_3_2:
                return PackageManager.USER_MIN_ASPECT_RATIO_3_2;
            case KEY_PREF_DISPLAY_SIZE:
                return PackageManager.USER_MIN_ASPECT_RATIO_DISPLAY_SIZE;
            default:
                return PackageManager.USER_MIN_ASPECT_RATIO_UNSET;
        }
    }

    @VisibleForTesting
    AspectRatioManager getAspectRatioManager() {
        return mAspectRatioManager;
    }
}
