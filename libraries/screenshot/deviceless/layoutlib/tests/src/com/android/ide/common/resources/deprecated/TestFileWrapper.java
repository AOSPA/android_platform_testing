/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.ide.common.resources.deprecated;

import com.android.io.FileWrapper;
import com.android.io.IAbstractFolder;

import java.io.File;

public class TestFileWrapper extends FileWrapper {
    public TestFileWrapper(File file) {
        super(file);
    }

    public IAbstractFolder getParentFolder() {
        String p = this.getParent();
        if (p == null) {
            return null;
        }
        return new TestFolderWrapper(p);
    }
}
