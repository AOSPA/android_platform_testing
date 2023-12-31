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
package android.support.test.launcherhelper2;

import android.graphics.Point;

import androidx.test.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.android.launcher3.tapl.BaseOverview;
import com.android.launcher3.tapl.LauncherInstrumentation;

import junit.framework.Assert;

import java.io.IOException;

/** Implementation of {@link ILauncherStrategy} to support Nexus launcher */
public class NexusLauncherStrategy extends BaseLauncher3Strategy {

    private static final String LAUNCHER_PKG = "com.google.android.apps.nexuslauncher";
    private LauncherInstrumentation mLauncher;

    @Override
    public void setUiDevice(UiDevice uiDevice) {
        super.setUiDevice(uiDevice);
        try {
            uiDevice.executeShellCommand(
                    "settings put secure swipe_up_to_switch_apps_enabled "
                            + (isOreoOrAbove() ? 1 : 0));
        } catch (IOException e) {
            Assert.fail("Failed to set swipe_up_to_switch_apps_enabled, caused by: " + e);
        }
        try {
            mLauncher = new LauncherInstrumentation(InstrumentationRegistry.getInstrumentation());

        } catch (IllegalStateException | NoClassDefFoundError e) {
            mLauncher =
                    new LauncherInstrumentation(
                            androidx.test.InstrumentationRegistry.getInstrumentation());
        }
    }

    @Override
    public String getSupportedLauncherPackage() {
        return LAUNCHER_PKG;
    }

    /** {@inheritDoc} */
    @Override
    public BySelector getAllAppsSelector() {
        return By.res(getSupportedLauncherPackage(), "apps_view");
    }

    /** {@inheritDoc} */
    @Override
    public BySelector getAllAppsButtonSelector() {
        throw new UnsupportedOperationException("UI element no longer exists.");
    }

    /** {@inheritDoc} */
    @Override
    public BySelector getHotSeatSelector() {
        return By.res(getSupportedLauncherPackage(), "hotseat");
    }

    private BySelector getLauncherOverviewSelector() {
        return By.res(LAUNCHER_PKG, "overview_panel");
    }

    @Override
    public void open() {
        mDevice.pressHome();
    }

    /** {@inheritDoc} */
    @Override
    public UiObject2 openAllApps(boolean reset) {
        // If not on all apps or to reset, then go to launcher and re-open all apps.
        if (!mDevice.hasObject(getAllAppsSelector())
                || mDevice.hasObject(getLauncherOverviewSelector())
                || reset) {
            // Restart from the launcher home screen.
            open();
            mDevice.waitForIdle();
            Assert.assertTrue(
                    "openAllApps: can't go to home screen",
                    !mDevice.hasObject(getAllAppsSelector())
                            && !mDevice.hasObject(getLauncherOverviewSelector()));
            if (isOreoOrAbove()) {
                int midX = mDevice.getDisplayWidth() / 2;
                int height = mDevice.getDisplayHeight();
                // Swipe from 6/7ths down the screen to 1/7th down the screen.
                mDevice.swipe(
                        midX,
                        height * 6 / 7,
                        midX,
                        height / 7,
                        (height * 2 / 3) / 100); // 100 px/step
            } else {
                // Swipe from the hotseat to near the top, e.g. 10% of the screen.
                UiObject2 hotseat = mDevice.wait(Until.findObject(getHotSeatSelector()), 2500);
                Point start = hotseat.getVisibleCenter();
                int endY = (int) (mDevice.getDisplayHeight() * 0.1f);
                mDevice.swipe(
                        start.x, start.y, start.x, endY, (start.y - endY) / 100); // 100 px/step
            }
        }
        UiObject2 allAppsContainer = mDevice.wait(Until.findObject(getAllAppsSelector()), 2500);
        Assert.assertNotNull("openAllApps: did not find all apps container", allAppsContainer);
        return allAppsContainer;
    }

    /** {@inheritDoc} */
    @Override
    public void openOverview() {
        mLauncher.goHome().switchToOverview();
    }

    /** {@inheritDoc} */
    @Override
    public boolean clearRecentAppsFromOverview() {
        if (!isInOverview()) {
            openOverview();
        }

        BaseOverview overview = mLauncher.getOverview();
        if (overview.hasTasks()) {
            overview.dismissAllTasks();
        }

        return !overview.hasTasks();
    }

    /** {@inheritDoc} */
    @Override
    public UiObject2 openAllWidgets(boolean reset) {
        if (!mDevice.hasObject(getAllWidgetsSelector())) {
            open();
            // trigger the wallpapers/widgets/settings view
            mDevice.pressMenu();
            mDevice.waitForIdle();
            UiObject2 optionsPopup =
                    mDevice.findObject(
                            By.res(getSupportedLauncherPackage(), "deep_shortcuts_container"));
            optionsPopup.findObject(By.text("Widgets")).click();
        }
        UiObject2 allWidgetsContainer =
                mDevice.wait(Until.findObject(getAllWidgetsSelector()), 2000);
        Assert.assertNotNull(
                "openAllWidgets: did not find all widgets container", allWidgetsContainer);
        if (reset) {
            CommonLauncherHelper.getInstance(mDevice)
                    .scrollBackToBeginning(
                            allWidgetsContainer, Direction.reverse(getAllWidgetsScrollDirection()));
        }
        return allWidgetsContainer;
    }

    /** {@inheritDoc} */
    @Override
    public long launch(String appName, String packageName) {
        return CommonLauncherHelper.getInstance(mDevice).launchApp(mLauncher, appName, packageName);
    }
}
