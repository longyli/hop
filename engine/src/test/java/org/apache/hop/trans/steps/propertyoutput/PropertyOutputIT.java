/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.propertyoutput;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.Props;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;

public class PropertyOutputIT {
  
  @Before
  public void setUp() throws Exception {
    HopClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }
  
  @After
  public void tearDown() throws Exception {

  }
  
  @Test
  public void testExecute() throws HopException, IOException {
    TransMeta meta = new TransMeta( getClass().getResource( "propertyOutput.ktr" ).getPath() );
    Trans trans = new Trans( meta );
    trans.execute( new String[] {} );   
    trans.waitUntilFinished();
    
    //check that trans is finished
    assertTrue( trans.isFinished() );
    
    PropertyOutputData dataStep = (PropertyOutputData) trans.getSteps().get( 1 ).data;
    
    RandomAccessFile fos = null;
    try {
      File file = new File( URI.create( dataStep.filename.replace( "\\", "/" ) ).getPath() );
      if ( file.exists() ) {
        fos = new RandomAccessFile( file, "rw" );
      }
    } catch ( FileNotFoundException | SecurityException e ) {
      fail( "the file with properties should be unallocated" );
    } finally {
      if ( fos != null ) {
        fos.close();
      }
    }
  }

}
