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

package com.android.car.settings.qc;

import android.car.drivingstate.CarUxRestrictions;
import android.car.drivingstate.CarUxRestrictionsManager.OnUxRestrictionsChangedListener;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import com.android.car.datasubscription.DataSubscription;
import com.android.car.settings.common.CarUxRestrictionsHelper;

import java.io.IOException;

/**
 * Base background worker for mobile data QCItems.
 * @param <E> The {@link SettingsQCItem} the background worker is associated with.
 */
public abstract class MobileDataBaseWorker<E extends SettingsQCItem>
        extends SettingsQCBackgroundWorker<E> implements OnUxRestrictionsChangedListener {

    private final TelephonyManager mTelephonyManager;
    private final int mSubId;
    private final SignalStrengthsListener mSignalStrengthsListener;
    private final DataSubscription mSubscription;
    private CarUxRestrictionsHelper mUxRestrictionsHelper;
    private final DataSubscription.DataSubscriptionChangeListener mDataSubscriptionChangeListener =
            new DataSubscription.DataSubscriptionChangeListener() {
                @Override
                public void onChange(int value) {
                    if (getQCItem() != null) {
                        ((MobileDataRow) getQCItem()).setSubscriptionStatus(value);
                        notifyQCItemChange();
                    }
                }
            };
    private boolean mCallbacksRegistered;

    private final ContentObserver mMobileDataChangeObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            notifyQCItemChange();
        }
    };

    protected MobileDataBaseWorker(Context context, Uri uri) {
        super(context, uri);
        mTelephonyManager = context.getSystemService(TelephonyManager.class);
        mSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        mSignalStrengthsListener = new SignalStrengthsListener();
        mSubscription = new DataSubscription(context);
        mUxRestrictionsHelper = new CarUxRestrictionsHelper(/* context= */ context, /* listener= */
                this);
    }

    @Override
    protected void onQCItemSubscribe() {
        if (mSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID && !mCallbacksRegistered) {
            mTelephonyManager.registerTelephonyCallback(getContext().getMainExecutor(),
                    mSignalStrengthsListener);
            getContext().getContentResolver().registerContentObserver(getObservableUri(mSubId),
                    /* notifyForDescendants= */ false, mMobileDataChangeObserver);
            mCallbacksRegistered = true;
        }
        mSubscription.addDataSubscriptionListener(mDataSubscriptionChangeListener);
        ((MobileDataRow) getQCItem()).setCarUxRestrictions(
                mUxRestrictionsHelper.getCarUxRestrictions());
    }

    @Override
    protected void onQCItemUnsubscribe() {
        if (mSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            unregisterCallbacks();
        }
        mSubscription.removeDataSubscriptionListener();
    }

    @Override
    public void close() throws IOException {
        if (mSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            unregisterCallbacks();
        }
        mUxRestrictionsHelper.destroy();
    }

    private void unregisterCallbacks() {
        if (mCallbacksRegistered) {
            mTelephonyManager.unregisterTelephonyCallback(mSignalStrengthsListener);
            getContext().getContentResolver().unregisterContentObserver(mMobileDataChangeObserver);
            mCallbacksRegistered = false;
        }
    }

    private Uri getObservableUri(int subId) {
        Uri uri = Settings.Global.getUriFor(Settings.Global.MOBILE_DATA);
        if (TelephonyManager.from(getContext()).getSimCount() != 1) {
            uri = Settings.Global.getUriFor(Settings.Global.MOBILE_DATA + subId);
        }
        return uri;
    }

    private class SignalStrengthsListener extends TelephonyCallback
            implements TelephonyCallback.SignalStrengthsListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            notifyQCItemChange();
        }
    }
    @Override
    public void onUxRestrictionsChanged(CarUxRestrictions restrictionInfo) {
        if (getQCItem() != null) {
            ((MobileDataRow) getQCItem()).setCarUxRestrictions(restrictionInfo);
            notifyQCItemChange();
        }
    }
}