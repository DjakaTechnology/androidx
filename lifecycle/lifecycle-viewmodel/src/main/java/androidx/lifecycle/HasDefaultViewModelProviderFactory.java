/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.viewmodel.CreationExtras;

/**
 * Interface that marks a {@link ViewModelStoreOwner} as having a default
 * {@link androidx.lifecycle.ViewModelProvider.Factory} for use with
 * {@link androidx.lifecycle.ViewModelProvider#ViewModelProvider(ViewModelStoreOwner)}.
 */
public interface HasDefaultViewModelProviderFactory {
    /**
     * Returns the default {@link androidx.lifecycle.ViewModelProvider.Factory} that should be
     * used when no custom {@code Factory} is provided to the
     * {@link androidx.lifecycle.ViewModelProvider} constructors.
     *
     * @return a {@code ViewModelProvider.Factory}
     */
    @NonNull
    ViewModelProvider.Factory getDefaultViewModelProviderFactory();

    /**
     * Returns the default {@link CreationExtras} that should be passed into the
     * {@link ViewModelProvider.Factory#create(Class, CreationExtras)} when no overriding
     * {@link CreationExtras} were passed to the
     * {@link androidx.lifecycle.ViewModelProvider} constructors.
     */
    @NonNull
    default CreationExtras getDefaultViewModelCreationExtras() {
        return CreationExtras.Empty.INSTANCE;
    }
}
