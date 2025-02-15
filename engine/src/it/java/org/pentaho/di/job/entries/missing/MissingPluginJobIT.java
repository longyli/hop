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

package org.apache.hop.job.entries.missing;

import org.junit.Before;
import org.junit.Test;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.Result;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.job.Job;
import org.apache.hop.job.JobMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;

public class MissingPluginJobIT {

  @Before
  public void setUp() throws HopException {
    HopEnvironment.init();
  }

  /**
   * Given a job having an entry which's plugin is missing in current Hop installation.
   * When this job is executed, then execution should fail.
   */
  @Test
  public void testForPluginMissingStep() throws Exception {
    InputStream is = new FileInputStream(
      new File( this.getClass().getResource( "missing_plugin_job.kjb" ).getFile() ) );

    JobMeta meta = new JobMeta( is, null, null );
    Job job = new Job( null, meta );

    Result result = new Result();
    job.execute( 0, result );
    assertFalse( result.getResult() );
  }
}
