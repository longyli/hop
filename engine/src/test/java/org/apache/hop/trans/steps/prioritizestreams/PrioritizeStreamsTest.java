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

package org.apache.hop.trans.steps.prioritizestreams;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.hop.core.QueueRowSet;
import org.apache.hop.core.RowSet;
import org.apache.hop.core.SingleRowRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.steps.mock.StepMockHelper;

public class PrioritizeStreamsTest {

  private static StepMockHelper<PrioritizeStreamsMeta, StepDataInterface> stepMockHelper;

  @BeforeClass
  public static void setup() {
    stepMockHelper =
        new StepMockHelper<PrioritizeStreamsMeta, StepDataInterface>( "Priority Streams Test",
            PrioritizeStreamsMeta.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void tearDown() {
    stepMockHelper.cleanUp();
  }

  /**
   * [PDI-9088] Prioritize streams step causing null pointer exception downstream 
   * @throws HopException
   */
  @Test
  public void testProcessRow() throws HopException {
    PrioritizeStreamsMeta meta = new PrioritizeStreamsMeta();
    meta.setStepName( new String[] { "high", "medium", "low" } );
    PrioritizeStreamsData data = new PrioritizeStreamsData();

    PrioritizeStreamsInner step = new PrioritizeStreamsInner( stepMockHelper );
    try {
      step.processRow( meta, data );
    } catch ( NullPointerException e ) {
      fail( "NullPointerException detecded, seems that RowMetaInterface was not set for RowSet you are attempting"
          + "to read from." );
    }

    Assert.assertTrue( "First waiting for row set is 'high'", data.currentRowSet.getClass().equals(
        SingleRowRowSet.class ) );
  }

  private class PrioritizeStreamsInner extends PrioritizeStreams {

    public PrioritizeStreamsInner( StepMockHelper<PrioritizeStreamsMeta, StepDataInterface> stepMockHelper ) {
      super( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
          stepMockHelper.trans );
    }

    @Override
    public RowSet findInputRowSet( String sourceStep ) throws HopStepException {
      if ( sourceStep.equals( "high" ) ) {
        return new SingleRowRowSet();
      }
      return new QueueRowSet();
    }

    @Override
    protected void checkInputLayoutValid( RowMetaInterface referenceRowMeta, RowMetaInterface compareRowMeta ) {
      // always true.
    }

    @Override
    public Object[] getRowFrom( RowSet rowSet ) throws HopStepException {
      rowSet.setRowMeta( new RowMeta() );
      return new Object[] {};
    }

    @Override
    public void putRow( RowMetaInterface rmi, Object[] input ) {
      if ( rmi == null ) {
        throw new NullPointerException();
      }
    }
  }
}
