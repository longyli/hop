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

package org.apache.hop.core.parameters;

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
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.trans.RowStepCollector;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransHopMeta;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.dummytrans.DummyTransMeta;
import org.apache.hop.trans.steps.getvariable.GetVariableMeta;
import org.apache.hop.trans.steps.getvariable.GetVariableMeta.FieldDefinition;

import junit.framework.TestCase;

/**
 * Test class for parameters in transformations.
 *
 * @author Sven Boden
 */
public class ParameterSimpleTransIT extends TestCase {
  public RowMetaInterface createResultRowMetaInterface1() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "PARAM1" ), new ValueMetaString( "PARAM2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface1();

    Object[] r1 = new Object[] { "ParamValue1", "PARAMVALUE2" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface2() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "PARAM1" ), new ValueMetaString( "PARAM2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData2() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface2();

    Object[] r1 = new Object[] { "ParamValue1", "default2" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface3() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaString( "${JAVA_HOME}" ),
      new ValueMetaString( "PARAM2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData3() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface3();

    Object[] r1 = new Object[] { "${JAVA_HOME}", "default2" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface5() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "PARAM1" ), new ValueMetaString( "PARAM2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData5() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface5();

    Object[] r1 = new Object[] { "default1", "PARAMVALUE2" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public RowMetaInterface createResultRowMetaInterface6() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "PARAM1" ), new ValueMetaString( "PARAM2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData6() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface5();

    Object[] r1 = new Object[] { "", "PARAMVALUE2" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
   *
   * @param rows1
   *          first row set to compare
   * @param rows2
   *          second row set to compare
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

      if ( rm1.size() != rm2.size() ) {
        fail( "row nr " + idx + " is not equal" );
      }
      int[] fields = new int[rm1.size()];
      for ( int ydx = 0; ydx < rm1.size(); ydx++ ) {
        fields[ydx] = ydx;
      }
      try {
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + " is not equal" );
        }
      } catch ( HopValueException e ) {
        fail( "row nr " + idx + " is not equal" );
      }

      idx++;
    }
  }

  /**
   * Test case for parameters using a simple transformation.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans1() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans1" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "PARAM1", "PARAM2" };
    String[] varName = { "${Param1}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "Param1", "", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "", "Parameter 2" );
    trans.setParameterValue( "Param1", "ParamValue1" );
    trans.setParameterValue( "PARAM2", "PARAMVALUE2" );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData1();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  /**
   * Test case for parameters using a simple transformation. Here 1 parameter is not provided as value, so the default
   * will be used.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans2() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans2" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "Param1", "PARAM2" };
    String[] varName = { "${Param1}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "Param1", "default1", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "default2", "Parameter 2" );
    trans.setParameterValue( "Param1", "ParamValue1" );
    // PARAM2 is not set

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData2();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  /**
   * Test case for parameters using a simple transformation. Here blocking some unwise usage of parameters.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans3() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans3" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "PARAM1", "PARAM2" };
    String[] varName = { "${JAVA_HOME}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "${JAVA_HOME}", "default1", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "default2", "Parameter 2" );
    trans.setParameterValue( "${JAVA_HOME}", "param1" );
    // PARAM2 is not set

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData3();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  /**
   * Test case for parameters using a simple transformation. Check whether parameters override variables.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans4() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans4" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "PARAM1", "PARAM2" };
    String[] varName = { "${Param1}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "Param1", "", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "", "Parameter 2" );
    trans.setParameterValue( "Param1", "ParamValue1" );
    trans.setParameterValue( "PARAM2", "PARAMVALUE2" );

    // See whether this variable overrides the parameter... it should NOT.
    trans.setVariable( "Param1", "Variable1" );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData1();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  /**
   * Test case for parameters using a simple transformation. Check whether parameters override variables.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans5() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans4" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "PARAM1", "PARAM2" };
    String[] varName = { "${Param1}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "Param1", "default1", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "", "Parameter 2" );
    trans.setParameterValue( "PARAM2", "PARAMVALUE2" );

    // See whether this variable overrides the parameter... it should NOT. Param1
    // is defined but not set, so defaults should kick in.
    trans.setVariable( "Param1", "Variable1" );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData5();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  /**
   * Test case for parameters using a simple transformation. Check whether parameters override variables.
   *
   * @throws Exception
   *           exception on any problem.
   */
  public void testParameterSimpleTrans6() throws Exception {
    HopEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "parameter_simple_trans4" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create a get variables step...
    //
    String getVariablesStepname = "get variables step";
    GetVariableMeta gvm = new GetVariableMeta();

    // Set the information of the get variables step.
    String getVariablesPid = registry.getPluginId( StepPluginType.class, gvm );
    StepMeta getVariablesStep = new StepMeta( getVariablesPid, getVariablesStepname, gvm );
    transMeta.addStep( getVariablesStep );

    //
    // Generate 1 row
    //
    String[] fieldName = { "PARAM1", "PARAM2" };
    String[] varName = { "${Param1}", "%%PARAM2%%" };
    int[] fieldType = { ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_STRING };
    int[] length = { -1, -1 };
    int[] precision = { -1, -1 };
    String[] format = { "", "" };
    String[] currency = { "", "" };
    String[] decimal = { "", "" };
    String[] grouping = { "", "" };
    int[] trimType = { ValueMetaInterface.TRIM_TYPE_NONE, ValueMetaInterface.TRIM_TYPE_NONE };

    FieldDefinition[] fields = new FieldDefinition[fieldName.length];
    for ( int i = 0; i < fields.length; i++ ) {
      FieldDefinition field = new FieldDefinition();
      field.setFieldName( fieldName[i] );
      field.setVariableString( varName[i] );
      field.setFieldType( fieldType[i] );
      field.setFieldLength( length[i] );
      field.setFieldPrecision( precision[i] );
      field.setFieldFormat( format[i] );
      field.setCurrency( currency[i] );
      field.setDecimal( decimal[i] );
      field.setGroup( grouping[i] );
      field.setTrimType( trimType[i] );
      fields[i] = field;
    }
    gvm.setFieldDefinitions( fields );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi1 = new TransHopMeta( getVariablesStep, dummyStep1 );
    transMeta.addTransHop( hi1 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.addParameterDefinition( "Param1", "", "Parameter 1" );
    trans.addParameterDefinition( "PARAM2", "", "Parameter 2" );
    trans.setParameterValue( "PARAM2", "PARAMVALUE2" );

    // See whether this variable overrides the parameter... it should NOT. Param1
    // is defined but not set. And no default... so the variable will be set to "". not
    // to "Variable1"
    trans.setVariable( "Param1", "Variable1" );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();

    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData6();
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }
}
