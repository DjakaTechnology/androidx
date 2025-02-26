/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.camera.extensions.internal;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.os.SystemClock;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.camera.camera2.impl.Camera2ImplConfig;
import androidx.camera.camera2.impl.CameraEventCallback;
import androidx.camera.camera2.impl.CameraEventCallbacks;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.Logger;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.CaptureConfig;
import androidx.camera.core.impl.Config;
import androidx.camera.core.impl.ConfigProvider;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.impl.CaptureStageImpl;
import androidx.camera.extensions.impl.PreviewExtenderImpl;
import androidx.camera.extensions.impl.PreviewImageProcessorImpl;
import androidx.core.util.Preconditions;

import java.util.List;

/**
 * For providing extensions config for preview.
 */
@RequiresApi(21) // TODO(b/200306659): Remove and replace with annotation on package-info.java
public class PreviewConfigProvider implements ConfigProvider<PreviewConfig> {
    private static final String TAG = "PreviewConfigProvider";
    static final Config.Option<Integer> OPTION_PREVIEW_CONFIG_PROVIDER_MODE = Config.Option.create(
            "camerax.extensions.previewConfigProvider.mode", Integer.class);
    private final VendorExtender mVendorExtender;
    private final Context mContext;
    @ExtensionMode.Mode
    private final int mEffectMode;

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    public PreviewConfigProvider(
            @ExtensionMode.Mode int mode,
            @NonNull VendorExtender vendorExtender,
            @NonNull Context context) {
        mEffectMode = mode;
        mVendorExtender = vendorExtender;
        mContext = context;
    }

    @NonNull
    @Override
    public PreviewConfig getConfig() {
        Preview.Builder builder = new Preview.Builder();
        updateBuilderConfig(builder, mEffectMode, mVendorExtender, mContext);

        return builder.getUseCaseConfig();
    }

    /**
     * Update extension related configs to the builder.
     */
    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    void updateBuilderConfig(@NonNull Preview.Builder builder,
            @ExtensionMode.Mode int effectMode, @NonNull VendorExtender vendorExtender,
            @NonNull Context context) {
        if (vendorExtender instanceof BasicVendorExtender) {
            PreviewExtenderImpl previewExtenderImpl =
                    ((BasicVendorExtender) vendorExtender).getPreviewExtenderImpl();

            if (previewExtenderImpl != null) {
                PreviewEventAdapter previewEventAdapter;

                switch (previewExtenderImpl.getProcessorType()) {
                    case PROCESSOR_TYPE_REQUEST_UPDATE_ONLY:
                        AdaptingRequestUpdateProcessor adaptingRequestUpdateProcessor =
                                new AdaptingRequestUpdateProcessor(previewExtenderImpl);
                        builder.setImageInfoProcessor(adaptingRequestUpdateProcessor);
                        previewEventAdapter = new PreviewEventAdapter(previewExtenderImpl, context,
                                adaptingRequestUpdateProcessor);
                        break;
                    case PROCESSOR_TYPE_IMAGE_PROCESSOR:
                        AdaptingPreviewProcessor adaptingPreviewProcessor = new
                                AdaptingPreviewProcessor(
                                (PreviewImageProcessorImpl) previewExtenderImpl.getProcessor());
                        builder.setCaptureProcessor(adaptingPreviewProcessor);
                        builder.setIsRgba8888SurfaceRequired(true);
                        previewEventAdapter = new PreviewEventAdapter(previewExtenderImpl, context,
                                adaptingPreviewProcessor);
                        break;
                    default:
                        previewEventAdapter = new PreviewEventAdapter(previewExtenderImpl, context,
                                null);
                }
                new Camera2ImplConfig.Extender<>(builder).setCameraEventCallback(
                        new CameraEventCallbacks(previewEventAdapter));
                builder.setUseCaseEventCallback(previewEventAdapter);
            } else {
                Logger.e(TAG, "PreviewExtenderImpl is null!");
            }
        } else { // Advanced extensions interface.
            // Set RGB8888 = true always since we have no way to tell if the OEM implementation does
            // the processing or not.
            builder.setIsRgba8888SurfaceRequired(true);
        }

        builder.getMutableConfig().insertOption(OPTION_PREVIEW_CONFIG_PROVIDER_MODE, effectMode);
        List<Pair<Integer, Size[]>> supportedResolutions =
                vendorExtender.getSupportedPreviewOutputResolutions();
        builder.setSupportedResolutions(supportedResolutions);
    }

    /**
     * An implementation to adapt the OEM provided implementation to core.
     */
    private static class PreviewEventAdapter extends CameraEventCallback implements
            UseCase.EventCallback {
        @NonNull
        final PreviewExtenderImpl mImpl;
        @NonNull
        private final Context mContext;
        @Nullable
        private final VendorProcessor mPreviewProcessor;
        @Nullable
        CameraInfo mCameraInfo;
        private long mOnEnableSessionTimeStamp = 0;
        private static final long MIN_DURATION_FOR_ENABLE_DISABLE_SESSION = 100L;

        @GuardedBy("mLock")
        final Object mLock = new Object();

        PreviewEventAdapter(@NonNull PreviewExtenderImpl impl,
                @NonNull Context context, @Nullable VendorProcessor previewProcessor) {
            mImpl = impl;
            mContext = context;
            mPreviewProcessor = previewProcessor;
        }

        // Invoked from main thread
        @Override
        public void onAttach(@NonNull CameraInfo cameraInfo) {
            synchronized (mLock) {
                mCameraInfo = cameraInfo;
            }
        }

        // Invoked from main thread
        @Override
        public void onDetach() {
            synchronized (mLock) {
                if (mPreviewProcessor != null) {
                    mPreviewProcessor.close();
                }
            }
        }

        // Invoked from camera thread
        @Override
        @Nullable
        @OptIn(markerClass = ExperimentalCamera2Interop.class)
        public CaptureConfig onInitSession() {
            synchronized (mLock) {
                Preconditions.checkNotNull(mCameraInfo,
                        "PreviewConfigProvider was not attached.");
                String cameraId = Camera2CameraInfo.from(mCameraInfo).getCameraId();
                CameraCharacteristics cameraCharacteristics =
                        Camera2CameraInfo.extractCameraCharacteristics(mCameraInfo);
                Logger.d(TAG, "Preview onInit");
                mImpl.onInit(cameraId, cameraCharacteristics, mContext);
                if (mPreviewProcessor != null) {
                    mPreviewProcessor.onInit();
                }
                CaptureStageImpl captureStageImpl = mImpl.onPresetSession();
                if (captureStageImpl != null) {
                    if (Build.VERSION.SDK_INT >= 28) {
                        return new AdaptingCaptureStage(captureStageImpl).getCaptureConfig();
                    } else {
                        Logger.w(TAG, "The CaptureRequest parameters returned from "
                                + "onPresetSession() will be passed to the camera device as part "
                                + "of the capture session via "
                                + "SessionConfiguration#setSessionParameters(CaptureRequest) "
                                + "which only supported from API level 28!");
                    }
                }
            }

            return null;
        }

        // Invoked from camera thread
        @Override
        @Nullable
        public CaptureConfig onEnableSession() {
            synchronized (mLock) {
                Logger.d(TAG, "Preview onEnableSession");
                CaptureStageImpl captureStageImpl = mImpl.onEnableSession();
                mOnEnableSessionTimeStamp = SystemClock.elapsedRealtime();
                if (captureStageImpl != null) {
                    return new AdaptingCaptureStage(captureStageImpl).getCaptureConfig();
                }
            }

            return null;
        }

        // Invoked from camera thread
        @Override
        @Nullable
        public CaptureConfig onDisableSession() {
            synchronized (mLock) {
                ensureMinDurationAfterOnEnableSession();
                Logger.d(TAG, "Preview onDisableSession");
                CaptureStageImpl captureStageImpl = mImpl.onDisableSession();
                if (captureStageImpl != null) {
                    return new AdaptingCaptureStage(captureStageImpl).getCaptureConfig();
                }
            }

            return null;
        }

        /**
         * Ensures onDisableSession not invoked too soon after onDisableSession. OEMs usually
         * releases resources at onDisableSession. Invoking onDisableSession too soon might cause
         * some crash during the initialization triggered by onEnableSession or onInit.
         */
        private void ensureMinDurationAfterOnEnableSession() {
            long timeAfterOnEnableSession =
                    SystemClock.elapsedRealtime() - mOnEnableSessionTimeStamp;
            if (timeAfterOnEnableSession < MIN_DURATION_FOR_ENABLE_DISABLE_SESSION) {
                try {
                    long timeToWait =
                            MIN_DURATION_FOR_ENABLE_DISABLE_SESSION - timeAfterOnEnableSession;
                    Logger.d(TAG, "onDisable too soon, wait " + timeToWait + " ms");
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    Logger.e(TAG, "sleep interrupted");
                }
            }
        }

        // Invoked from camera thread
        @Override
        @Nullable
        public CaptureConfig onRepeating() {
            synchronized (mLock) {
                CaptureStageImpl captureStageImpl = mImpl.getCaptureStage();
                if (captureStageImpl != null) {
                    return new AdaptingCaptureStage(captureStageImpl).getCaptureConfig();
                }
            }

            return null;
        }

        // Invoked from camera thread
        @Override
        public void onDeInitSession() {
            synchronized (mLock) {
                Logger.d(TAG, "Preview onDeInit");
                if (mPreviewProcessor != null) {
                    mPreviewProcessor.onDeInit();
                }
                mImpl.onDeInit();
            }
        }
    }
}
