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

package org.apache.hop.trans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.dummytrans.DummyTransMeta;
import org.apache.hop.trans.steps.injector.InjectorMeta;

import junit.framework.TestCase;

/**
 * Test class for the use of hops, specifically we want to check the copy and distribute mode.
 *
 * @author Sven Boden
 */
public class HopIT extends TestCase {

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaInteger( "field1" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { new Long( 1L ) };
    Object[] r2 = new Object[] { new Long( 2L ) };
    Object[] r3 = new Object[] { new Long( 3L ) };
    Object[] r4 = new Object[] { new Long( 4L ) };
    Object[] r5 = new Object[] { new Long( 5L ) };
    Object[] r6 = new Object[] { new Long( 6L ) };
    Object[] r7 = new Object[] { new Long( 7L ) };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );
    list.add( new RowMetaAndData( rm, r5 ) );
    list.add( new RowMetaAndData( rm, r6 ) );
    list.add( new RowMetaAndData( rm, r7 ) );

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) {
    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      fail( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> it1 = rows1.iterator();
    Iterator<RowMetaAndData> it2 = rows2.iterator();

    while ( it1.hasNext() && it2.hasNext() ) {
      RowMetaAndData rm1 = it1.next();
      RowMetaAndData rm2 = it2.next();

      Object[] r1 = rm1.getData();
      Object[] r2 = rm2.getData();

      if ( r1.length != r2.length ) {
        fail( "row nr " + idx + "is not equal" );
      }
      int[] fields = new int[r1.length];
      for ( int ydx = 0; ydx < r1.length; ydx++ ) {
        fields[ydx] = ydx;
      }
      try {
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + "is not equal" );
        }
      } catch ( HopValueException e ) {
        fail( "row nr " + idx + "is not equal" );
      }

      idx++;
    }
  }

  /**
   * Test case for hop using copy.
   *
   * The transformation is as follows: an injector step links to a dummy step, which in turn links to 2 target dummy
   * steps.
   *
   * Both dummy1 and dummy2 should get all rows.
   */
  public void testCopyHops() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "hop test default" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep );
    transMeta.addTransHop( hi );

    // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
    dummyStep.setDistributes( false );

    //
    // Create a dummy target step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hop1 = new TransHopMeta( dummyStep, dummyStep1 );
    transMeta.addTransHop( hop1 );

    //
    // Create a dummy target step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hop2 = new TransHopMeta( dummyStep, dummyStep2 );
    transMeta.addTransHop( hop2 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si1 = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector rc1 = new RowStepCollector();
    si1.addRowListener( rc1 );

    StepInterface si2 = trans.getStepInterface( dummyStepname2, 0 );
    RowStepCollector rc2 = new RowStepCollector();
    si2.addRowListener( rc2 );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();
    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = rc1.getRowsWritten();
    checkRows( resultRows, inputList );
  }

  /**
   * Test case for hop use.
   *
   * The transformation is as follows: an injector step links to a dummy step, which in turn links to 2 target dummy
   * steps.
   *
   * The default in the GUI of spoon is copy mode, but here it seems to be distribute.
   */
  public void testDefaultConfiguration() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "hop test default" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep );
    transMeta.addTransHop( hi );

    //
    // Create a dummy target step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hop1 = new TransHopMeta( dummyStep, dummyStep1 );
    transMeta.addTransHop( hop1 );

    //
    // Create a dummy target step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hop2 = new TransHopMeta( dummyStep, dummyStep2 );
    transMeta.addTransHop( hop2 );

    // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
    dummyStep.setDistributes( true );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si1 = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector rc1 = new RowStepCollector();
    si1.addRowListener( rc1 );

    StepInterface si2 = trans.getStepInterface( dummyStepname2, 0 );
    RowStepCollector rc2 = new RowStepCollector();
    si2.addRowListener( rc2 );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> compareList1 = new ArrayList<RowMetaAndData>();
    List<RowMetaAndData> compareList2 = new ArrayList<RowMetaAndData>();
    int counter = 1;

    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
      if ( counter % 2 == 0 ) {
        compareList2.add( rm );
      } else {
        compareList1.add( rm );
      }
      counter++;
    }
    rp.finished();

    trans.waitUntilFinished();

    // Dummy1 should get 4 rows: 1 3 5 7
    // Dummy2 should get 3 rows: 2 4 6

    List<RowMetaAndData> resultRows1 = rc1.getRowsWritten();
    checkRows( resultRows1, compareList1 );

    List<RowMetaAndData> resultRows2 = rc2.getRowsWritten();
    checkRows( resultRows2, compareList2 );
  }

  /**
   * Test case for hop use.
   *
   * The transformation is as follows: an injector step links to a dummy step, which in turn links to 2 target dummy
   * steps.
   *
   * This testcase uses distribute mode, so each hop in turn should get a row.
   */
  public void testDistributeHops() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "hop test default" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.

    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep );
    transMeta.addTransHop( hi );

    //
    // Create a dummy target step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hop1 = new TransHopMeta( dummyStep, dummyStep1 );
    transMeta.addTransHop( hop1 );

    //
    // Create a dummy target step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hop2 = new TransHopMeta( dummyStep, dummyStep2 );
    transMeta.addTransHop( hop2 );

    // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
    dummyStep.setDistributes( true );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si1 = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector rc1 = new RowStepCollector();
    si1.addRowListener( rc1 );

    StepInterface si2 = trans.getStepInterface( dummyStepname2, 0 );
    RowStepCollector rc2 = new RowStepCollector();
    si2.addRowListener( rc2 );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> compareList1 = new ArrayList<RowMetaAndData>();
    List<RowMetaAndData> compareList2 = new ArrayList<RowMetaAndData>();
    int counter = 1;

    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
      if ( counter % 2 == 0 ) {
        compareList2.add( rm );
      } else {
        compareList1.add( rm );
      }
      counter++;
    }
    rp.finished();

    trans.waitUntilFinished();

    // Dummy1 should get 4 rows: 1 3 5 7
    // Dummy2 should get 3 rows: 2 4 6

    List<RowMetaAndData> resultRows1 = rc1.getRowsWritten();
    checkRows( resultRows1, compareList1 );

    List<RowMetaAndData> resultRows2 = rc2.getRowsWritten();
    checkRows( resultRows2, compareList2 );
  }
}
