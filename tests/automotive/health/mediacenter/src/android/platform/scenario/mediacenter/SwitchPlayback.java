/*
 * Copyright (C) 2023 The Android Open Source Project
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
package android.platform.test.scenario.mediacenter;

import android.platform.helpers.HelperAccessor;
import android.platform.helpers.IAutoMediaHelper;
import android.platform.test.scenario.annotation.Scenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@Scenario
@RunWith(JUnit4.class)
public class SwitchPlayback {
    static HelperAccessor<IAutoMediaHelper> sMediaCenterHelper =
            new HelperAccessor<>(IAutoMediaHelper.class);

    @Test
    public void testSwitchPlayback() {
        sMediaCenterHelper.get().minimizeNowPlaying();
        sMediaCenterHelper.get().maximizeNowPlaying();
    }
}
