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

package androidx.work;

import androidx.annotation.NonNull;

/**
 * An Exception Handler that can be used to determine if there were issues when trying
 * to schedule {@link WorkRequest}s.
 * <p>
 * This typically happens when the application exceeds its scheduling limits because it is using a
 * {@link  android.app.job.JobService} besides the one provided by {@link WorkManager}.
 */
public interface SchedulingExceptionHandler {
    /**
     * Allows the application to handle a {@link Throwable} throwable typically caused when
     * trying to schedule {@link WorkRequest}s.
     * <p>
     * This exception handler will be invoked a thread bound to
     * {@link Configuration#getTaskExecutor()}.
     *
     * @param throwable The underlying throwable that was caused when trying to schedule
     *                  {@link WorkRequest}s.
     */
    void handleException(@NonNull Throwable throwable);
}
