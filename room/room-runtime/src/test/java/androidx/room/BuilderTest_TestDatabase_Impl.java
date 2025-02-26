/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.room;

import androidx.annotation.NonNull;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BuilderTest_TestDatabase_Impl extends BuilderTest.TestDatabase {
    DatabaseConfiguration mConfig;
    List<Migration> mAutoMigrations = Arrays.asList(new BuilderTest.EmptyMigration(1, 2));

    @Override
    public void init(DatabaseConfiguration configuration) {
        super.init(configuration);
        mConfig = configuration;
    }

    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return Mockito.mock(SupportSQLiteOpenHelper.class);
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return Mockito.mock(InvalidationTracker.class);
    }

    @Override
    public void clearAllTables() {
    }

    @NonNull
    @Override
    public List<Migration> getAutoMigrations(
            @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs
    ) {
        return mAutoMigrations;
    }
}