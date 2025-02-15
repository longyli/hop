/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.cluster;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.hop.core.HopEnvironment;

public class CarteLauncherIT {

  @Test( timeout = 10 * 60 * 1000 )
  public void testLaunchStopSlaveServer() throws Exception {
    HopEnvironment.init();

    CarteLauncher carteLauncher = new CarteLauncher( "localhost", 8282 );
    Thread thread = new Thread( carteLauncher );
    thread.start();

    // Wait until the carte object is available...
    //
    while ( carteLauncher.getCarte() == null && !carteLauncher.isFailure() ) {
      Thread.sleep( 100 );
    }

    assertFalse( thread.isAlive() );
    assertTrue( carteLauncher.getCarte().getWebServer().getServer().isRunning() );
    carteLauncher.getCarte().getWebServer().stopServer();
    assertFalse( carteLauncher.getCarte().getWebServer().getServer().isRunning() );
  }
}
