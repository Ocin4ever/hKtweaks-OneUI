/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.hades.hKtweaks.utils;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;


/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper extends FingerprintManagerCompat.AuthenticationCallback {

    private final FingerprintManagerCompat mFingerprintManagerCompat;
    private final Callback mCallback;
    private boolean mListening;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;

    /**
     * Constructor for {@link FingerprintUiHelper}. This method is expected to be called from
     * only the {@link FingerprintUiHelperBuilder} class.
     */
    private FingerprintUiHelper(FingerprintManagerCompat fingerprintManagerCompat, Callback callback) {
        mFingerprintManagerCompat = fingerprintManagerCompat;
        mCallback = callback;
    }

    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (!mListening) {
            mListening = true;
            mCancellationSignal = new CancellationSignal();
            mSelfCancelled = false;
            mFingerprintManagerCompat.authenticate(cryptoObject, 0, mCancellationSignal, this, null);
        }
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            mListening = false;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            mCallback.onError();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {

        mCallback.onAuthenticated();
    }

    public interface Callback {
        void onAuthenticated();

        void onError();
    }

    /**
     * Builder class for {@link FingerprintUiHelper} in which injected fields from Dagger
     * holds its fields and takes other arguments in the {@link #build} method.
     */
    public static class FingerprintUiHelperBuilder {
        private final FingerprintManagerCompat mFingerprintManagerCompat;

        public FingerprintUiHelperBuilder(FingerprintManagerCompat fingerprintManagerCompat) {
            mFingerprintManagerCompat = fingerprintManagerCompat;
        }

        public FingerprintUiHelper build(Callback callback) {
            return new FingerprintUiHelper(mFingerprintManagerCompat, callback);
        }
    }
}
