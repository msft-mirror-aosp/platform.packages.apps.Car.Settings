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

import com.android.car.settings.Flags;
import com.android.car.settings.R;
import com.android.car.settings.common.SettingsFragment;

/**
 * Main page that hosts privacy-related Microphone preferences.
 */
public class MicrophoneSettingsFragment extends SettingsFragment {

    @Override
    protected int getPreferenceScreenResId() {
        // TODO(b/340636015): Update the name of the xml file when the flag is removed.
        if (Flags.microphonePrivacyUpdates()) {
            return R.xml.privacy_microphone_settings_fragment_updated;
        }
        return R.xml.privacy_microphone_settings_fragment;
    }
}
