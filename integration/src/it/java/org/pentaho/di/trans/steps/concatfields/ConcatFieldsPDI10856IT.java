/* ******************************************************************************
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

package org.apache.hop.trans.steps.concatfields;

import org.junit.Assert;
import org.junit.Test;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;

public class ConcatFieldsPDI10856IT {

  @Test
  public void rowLevelLoggingDoesNotFail() throws Exception {
    HopEnvironment.init();

    TransMeta transMeta = new TransMeta( "src/it/resources/org.apache.hop/trans/steps/concatfields/PDI-10856.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );

    Trans trans = new Trans( transMeta );
    trans.setLogLevel( LogLevel.ROWLEVEL );

    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    Assert.assertEquals(0, trans.getErrors());
  }
}
