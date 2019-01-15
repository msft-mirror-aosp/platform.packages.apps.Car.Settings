/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.car.settings.users;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import androidx.car.app.CarAlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.car.settings.R;

/**
 * Dialog to confirm user removal.
 */
public class ConfirmRemoveUserDialog extends DialogFragment {
    private final DialogInterface.OnClickListener mDeleteUserListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mListener != null && which == DialogInterface.BUTTON_POSITIVE) {
                mListener.onRemoveUserConfirmed();
            }
            dialog.dismiss();
        }
    };

    private ConfirmRemoveUserListener mListener;

    /**
     * Sets a listener for OnRemoveUserConfirmed that will get called if user confirms
     * the dialog.
     *
     * @param listener Instance of {@link ConfirmRemoveUserListener} to call when confirmed.
     */
    public void setConfirmRemoveUserListener(ConfirmRemoveUserListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CarAlertDialog.Builder(getContext())
            .setTitle(R.string.really_remove_user_title)
            .setBody(R.string.really_remove_user_message)
            .setPositiveButton(R.string.delete_button, mDeleteUserListener)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }

    /**
     * Interface for listeners that want to receive a callback when user confirms user removal in a
     * dialog.
     */
    public interface ConfirmRemoveUserListener {

        /**
         * Method called only when user presses delete button.
         */
        void onRemoveUserConfirmed();
    }
}
