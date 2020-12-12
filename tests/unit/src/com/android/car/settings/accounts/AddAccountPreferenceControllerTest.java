/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.car.settings.accounts;

import static com.android.car.settings.common.PreferenceController.AVAILABLE;
import static com.android.car.settings.common.PreferenceController.DISABLED_FOR_USER;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.settings.common.CarSettingActivities;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceControllerTestUtil;
import com.android.car.settings.testutils.TestLifecycleOwner;
import com.android.car.settings.users.UserHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class AddAccountPreferenceControllerTest {
    private Context mContext = spy(ApplicationProvider.getApplicationContext());
    private LifecycleOwner mLifecycleOwner;
    private Preference mPreference;
    private CarUxRestrictions mCarUxRestrictions;
    private AddAccountPreferenceController mController;

    @Mock
    private FragmentController mFragmentController;
    @Mock
    private UserHelper mMockUserHelper;
    @Mock
    private AccountTypesHelper mMockAccountTypesHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mLifecycleOwner = new TestLifecycleOwner();

        mCarUxRestrictions = new CarUxRestrictions.Builder(/* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_BASELINE, /* timestamp= */ 0).build();

        mPreference = new Preference(mContext);
        mController = new TestAddAccountPreferenceController(mContext,
                /* preferenceKey= */ "key", mFragmentController, mCarUxRestrictions);
        PreferenceControllerTestUtil.assignPreference(mController, mPreference);
        doNothing().when(mContext).startActivity(any());
    }

    @Test
    public void cannotModifyUsers_addAccountButtonShouldBeDisabled() {
        when(mMockUserHelper.canCurrentProcessModifyAccounts()).thenReturn(false);

        mController.onCreate(mLifecycleOwner);

        assertThat(mController.getAvailabilityStatus()).isEqualTo(DISABLED_FOR_USER);
    }

    @Test
    public void canModifyUsers_addAccountButtonShouldBeAvailable() {
        when(mMockUserHelper.canCurrentProcessModifyAccounts()).thenReturn(true);

        mController.onCreate(mLifecycleOwner);

        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void clickAddAccountButton_shouldOpenChooseAccountFragment() {
        when(mMockUserHelper.canCurrentProcessModifyAccounts()).thenReturn(true);

        mController.onCreate(mLifecycleOwner);
        mPreference.performClick();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(
                Intent.class);
        verify(mContext).startActivity(intentArgumentCaptor.capture());

        Intent intent = intentArgumentCaptor.getValue();
        assertThat(intent.getComponent().getClassName()).isEqualTo(
                CarSettingActivities.ChooseAccountActivity.class.getName());
    }

    @Test
    public void clickAddAccountButton_shouldNotOpenChooseAccountFragmentWhenOneType() {
        when(mMockUserHelper.canCurrentProcessModifyAccounts()).thenReturn(true);
        Set<String> accountSet = new HashSet<>();
        accountSet.add("TEST_ACCOUNT_TYPE_1");
        when(mMockAccountTypesHelper.getAuthorizedAccountTypes()).thenReturn(accountSet);

        mController.onCreate(mLifecycleOwner);
        mPreference.performClick();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(
                Intent.class);
        verify(mContext).startActivity(intentArgumentCaptor.capture());

        Intent intent = intentArgumentCaptor.getValue();
        assertThat(intent.getComponent().getClassName()).isEqualTo(
                AddAccountActivity.class.getName());
    }

    @Test
    public void clickAddAccountButton_shouldOpenChooseAccountFragmentWhenTwoTypes() {
        when(mMockUserHelper.canCurrentProcessModifyAccounts()).thenReturn(true);
        Set<String> accountSet = new HashSet<>();
        accountSet.add("TEST_ACCOUNT_TYPE_1");
        accountSet.add("TEST_ACCOUNT_TYPE_2");
        when(mMockAccountTypesHelper.getAuthorizedAccountTypes()).thenReturn(accountSet);

        mController.onCreate(mLifecycleOwner);
        mPreference.performClick();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(
                Intent.class);
        verify(mContext).startActivity(intentArgumentCaptor.capture());

        Intent intent = intentArgumentCaptor.getValue();
        assertThat(intent.getComponent().getClassName()).isEqualTo(
                CarSettingActivities.ChooseAccountActivity.class.getName());
    }

    private class TestAddAccountPreferenceController extends AddAccountPreferenceController {

        TestAddAccountPreferenceController(Context context, String preferenceKey,
                FragmentController fragmentController,
                CarUxRestrictions uxRestrictions) {
            super(context, preferenceKey, fragmentController, uxRestrictions);
        }

        @Override
        UserHelper getUserHelper() {
            return mMockUserHelper;
        }

        @Override
        AccountTypesHelper getAccountTypesHelper() {
            return mMockAccountTypesHelper;
        }
    }
}
