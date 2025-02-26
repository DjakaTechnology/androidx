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

package androidx.camera.core;

import static androidx.core.util.Preconditions.checkArgument;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of {@link UseCase}.
 *
 * When the {@link UseCaseGroup} is bound to {@link Lifecycle}, it binds all the
 * {@link UseCase}s to the same {@link Lifecycle}. {@link UseCase}s inside of a
 * {@link UseCaseGroup} usually share some common properties like the FOV defined by
 * {@link ViewPort}.
 */
@RequiresApi(21) // TODO(b/200306659): Remove and replace with annotation on package-info.java
public final class UseCaseGroup {

    @Nullable
    private final ViewPort mViewPort;

    @NonNull
    private final List<UseCase> mUseCases;

    @NonNull
    private final EffectBundle mEffectBundle;

    UseCaseGroup(@Nullable ViewPort viewPort, @NonNull List<UseCase> useCases,
            @NonNull EffectBundle effectBundle) {
        mViewPort = viewPort;
        mUseCases = useCases;
        mEffectBundle = effectBundle;
    }

    /**
     * Gets the {@link ViewPort} shared by the {@link UseCase} collection.
     */
    @Nullable
    public ViewPort getViewPort() {
        return mViewPort;
    }

    /**
     * Gets the {@link UseCase}s.
     */
    @NonNull
    public List<UseCase> getUseCases() {
        return mUseCases;
    }

    /**
     * Gets the {@link EffectBundle}.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @NonNull
    public EffectBundle getEffectBundle() {
        return mEffectBundle;
    }

    /**
     * A builder for generating {@link UseCaseGroup}.
     */
    public static final class Builder {

        private ViewPort mViewPort;

        private final List<UseCase> mUseCases;

        @Nullable
        private EffectBundle mEffectBundle;

        public Builder() {
            mUseCases = new ArrayList<>();
        }

        /**
         * Sets {@link ViewPort} shared by the {@link UseCase}s.
         */
        @NonNull
        public Builder setViewPort(@NonNull ViewPort viewPort) {
            mViewPort = viewPort;
            return this;
        }

        /**
         * Sets the {@link EffectBundle} for the {@link UseCase}s.
         *
         * <p>Once set, CameraX will use the {@link SurfaceEffect}s to process the outputs of
         * the {@link UseCase}s.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @NonNull
        public Builder setEffectBundle(@NonNull EffectBundle effectBundle) {
            mEffectBundle = effectBundle;
            return this;
        }

        /**
         * Adds {@link UseCase} to the collection.
         */
        @NonNull
        public Builder addUseCase(@NonNull UseCase useCase) {
            mUseCases.add(useCase);
            return this;
        }

        /**
         * Builds a {@link UseCaseGroup} from the current state.
         */
        @NonNull
        public UseCaseGroup build() {
            checkArgument(!mUseCases.isEmpty(), "UseCase must not be empty.");
            return new UseCaseGroup(mViewPort, mUseCases, mEffectBundle);
        }
    }

}
