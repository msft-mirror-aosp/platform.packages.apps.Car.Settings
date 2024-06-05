/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static com.android.car.settings.enterprise.EnterpriseUtils.getAvailabilityStatusRestricted;
import static com.android.car.settings.enterprise.EnterpriseUtils.hasUserRestrictionByDpm;
import static com.android.car.settings.enterprise.EnterpriseUtils.onClickWhileDisabled;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.hardware.SensorPrivacyManager;
import android.os.UserManager;

import com.android.car.settings.common.ColoredSwitchPreference;
import com.android.car.settings.common.FragmentController;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.camera.flags.Flags;

/** Business logic for controlling the mute camera toggle. */
public class CameraTogglePreferenceController
        extends CameraPrivacyBasePreferenceController<ColoredSwitchPreference> {

    public CameraTogglePreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController,
            CarUxRestrictions uxRestrictions) {
        this(context, preferenceKey, fragmentController, uxRestrictions,
                SensorPrivacyManager.getInstance(context));
    }

    @VisibleForTesting
    CameraTogglePreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions,
            SensorPrivacyManager sensorPrivacyManager) {
        super(context, preferenceKey, fragmentController, uxRestrictions, sensorPrivacyManager);
    }

    @Override
    protected Class<ColoredSwitchPreference> getPreferenceType() {
        return ColoredSwitchPreference.class;
    }

    @Override
    protected boolean handlePreferenceChanged(ColoredSwitchPreference preference,
            Object newValue) {
        boolean isChecked = (Boolean) newValue;
        boolean isCameraMuted = getSensorPrivacyManager().isSensorPrivacyEnabled(
                SensorPrivacyManager.Sensors.CAMERA);
        if (isChecked == isCameraMuted) {
            getSensorPrivacyManager().setSensorPrivacyForProfileGroup(
                    SensorPrivacyManager.Sources.SETTINGS,
                    SensorPrivacyManager.Sensors.CAMERA,
                    !isChecked);
        }
        return true;
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        if (Flags.cameraPrivacyAllowlist() || !getSensorPrivacyManager()
                .supportsSensorToggle(SensorPrivacyManager.Sensors.CAMERA)) {
            // Hide preference if feature flag is enabled or system doesn't have a camera
            return UNSUPPORTED_ON_DEVICE;
        } else {
            return getAvailabilityStatusRestricted(getContext(),
                    UserManager.DISALLOW_CAMERA_TOGGLE);
        }
    }

    @Override
    protected void updateState(ColoredSwitchPreference preference) {
        preference.setChecked(!getSensorPrivacyManager().isSensorPrivacyEnabled(
                SensorPrivacyManager.Sensors.CAMERA));
        if (hasUserRestrictionByDpm(getContext(), UserManager.DISALLOW_CAMERA_TOGGLE)) {
            setClickableWhileDisabled(preference, /* clickable= */ true, p ->
                    onClickWhileDisabled(getContext(), getFragmentController(),
                            UserManager.DISALLOW_CAMERA_TOGGLE));
        }
    }
}
