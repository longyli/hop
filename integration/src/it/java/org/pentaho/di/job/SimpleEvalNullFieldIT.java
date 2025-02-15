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

package org.apache.hop.job;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.hop.core.Result;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;

public class SimpleEvalNullFieldIT {
  private static String jobPath = "PDI-13387.kjb";
  private static String PKG = "org.apache.hop/job/";

  @BeforeClass
  public static void setUpBeforeClass() throws HopException {
    HopEnvironment.init();   
  }

  @Test
  public void testNullField() throws HopXMLException, IOException, URISyntaxException {
    JobMeta jm = new JobMeta( new File( SimultaneousJobsAppenderIT.class.getClassLoader().getResource( PKG + jobPath ).toURI() ).getCanonicalPath(), null );
    Job job = new Job( null, jm );    
    job.start();
    job.waitUntilFinished();
    Result result = job.getResult();

    Assert.assertTrue( result.getResult() );
    if ( result.getNrErrors() != 0 ) {
      Assert.fail( result.getLogText() );
    }
  }
}