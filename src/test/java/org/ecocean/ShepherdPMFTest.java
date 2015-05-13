/*
 * This file is a part of Wildbook.
 * Copyright (C) 2015 WildMe
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wildbook.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ecocean;

import java.util.*;

import com.samsix.database.*;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some ShepherdPMF routines.
 */
public class ShepherdPMFTest {

	@Test
    public void testGetDefaultConnectionInfo() {
        ConnectionInfo info = ShepherdPMF.getConnectionInfo();
        assertNotNull("No info for primary db", info);
        assertNotNull("No name for primary db", info.getUserName());
    }

	@Test
    public void testGetNamedConnectionInfo() {
        // we should always have a test db
        ConnectionInfo info = ShepherdPMF.getConnectionInfo("Test");
        assertNotNull("No info for test db", info);
        assertNotNull("No name for test db", info.getUserName());
    }
}
