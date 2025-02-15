/*! ****************************************************************************
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

package org.apache.hop.trans.steps.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.trans.RowProducer;
import org.apache.hop.trans.RowStepCollector;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransHopMeta;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.dummytrans.DummyTransMeta;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Regression test case for PDI-13072
 *
 * @author vladimir.dolzhenko@gmail.com
 */
public class RestInputIT {

  private static ServletTester tester;

  @BeforeClass
  public static void setUp() throws Exception {
    HopEnvironment.init();

    tester = new ServletTester();
    tester.setContextPath( "/context" );
    tester.setResourceBase( RestInputIT.class.getResource( "/" ).getFile() );
    final ServletHolder servletHolder = tester.addServlet( ServletContainer.class, "/*" );
    servletHolder.setInitParameter( "com.sun.jersey.config.property.classpath", "/" );
    servletHolder.setInitParameter( "jersey.config.server.provider.classnames", SimpleRestService.class.getName() );
    tester.start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if ( tester != null ) {
      tester.stop();
    }
  }

  protected Trans createAndTestTrans( PluginRegistry registry, TransMeta transMeta, StepMeta inputStep,
      RowStepCollector rowStepCollector, String name, int limit ) throws HopException {
    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi3 = new TransHopMeta( inputStep, dummyStep1 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    si.addRowListener( rowStepCollector );

    RowProducer rp = trans.addRowProducer( inputStep.getName(), 0 );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "pageSize" ) );
    rowMeta.addValueMeta( new ValueMetaString( "name" ) );
    rp.putRow( rowMeta, new Object[] { Integer.valueOf( limit ), name } );

    rp.finished();
    return trans;
  }

  public StepMeta createRestInputStep( TransMeta transMeta, PluginRegistry registry ) throws Exception {
    String inputName = "rest input step";
    RestMeta meta = new RestMeta();

    String inputPid = registry.getPluginId( StepPluginType.class, meta );
    StepMeta inputStep = new StepMeta( inputPid, inputName, meta );
    transMeta.addStep( inputStep );

    meta.setDefault();
    meta.setUrl( tester.createSocketConnector( true ) + "/context/simple/join" );
    meta.setMethod( "POST" );
    meta.setMatrixParameterField( new String[] { "pageSize" } );
    meta.setMatrixParameterName( new String[] { "limit" } );

    meta.setParameterField( new String[] { "name" } );
    meta.setParameterName( new String[] { "name" } );

    meta.setApplicationType( RestMeta.APPLICATION_TYPE_TEXT_PLAIN );
    meta.setFieldName( "result" );

    return inputStep;
  }

  @Test
  public void testRESTInput() throws Exception {
    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "restinput" );

    PluginRegistry registry = PluginRegistry.getInstance();

    StepMeta inputStep = createRestInputStep( transMeta, registry );

    RowStepCollector rowStepCollector = new RowStepCollector();
    final Trans trans = createAndTestTrans( registry, transMeta, inputStep, rowStepCollector, "limit", 5 );
    trans.startThreads();

    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = rowStepCollector.getRowsWritten();

    assertTrue( rowStepCollector.getRowsError().isEmpty() );
    assertEquals( 1, rowStepCollector.getRowsWritten().size() );

    final RowMetaAndData rowMetaAndData = resultRows.get( 0 );
    final RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();

    final String[] fieldNames = rowMeta.getFieldNames();
    final Object[] data = rowMetaAndData.getData();

    assertEquals( "pageSize", fieldNames[0] );
    assertEquals( "name", fieldNames[1] );
    assertEquals( "result", fieldNames[2] );

    assertEquals( Integer.valueOf( 5 ), data[0] );
    assertEquals( "limit", data[1] );
    assertEquals( "limit:5", data[2] );
  }

}
