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

package org.apache.hop.trans.steps.rowgenerator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.repository.ObjectId;
import org.apache.hop.repository.Repository;
import org.apache.hop.repository.StringObjectId;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;
import org.apache.hop.trans.steps.loadsave.MemoryRepository;
import org.apache.hop.trans.steps.loadsave.initializer.InitializerInterface;
import org.apache.hop.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.apache.hop.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.apache.hop.metastore.api.IMetaStore;

public class RowGeneratorMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  private Repository rep;

  private ObjectId id_step;

  private final String launchVariable = "${ROW_LIMIT}";

  private final String rowGeneratorRowLimitCode = "limit";
  private LoadSaveTester<?> loadSaveTester;
  private Class<RowGeneratorMeta> testMetaClass = RowGeneratorMeta.class;

  @Before
  public void setUp() throws HopException {
    rep = new MemoryRepository();
    id_step = new StringObjectId( "aStringObjectID" );
    rep.saveStepAttribute( new StringObjectId( "transId" ), id_step, rowGeneratorRowLimitCode, launchVariable );
  }

  /**
   * If we can read row limit as string from repository then we can run row generator.
   * @see RowGeneratorTest
   * @throws HopException
   */
  @Test
  public void testReadRowLimitAsStringFromRepository() throws HopException {
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    IMetaStore metaStore = Mockito.mock( IMetaStore.class );
    DatabaseMeta dbMeta = Mockito.mock( DatabaseMeta.class );
    rowGeneratorMeta.readRep( rep, metaStore, id_step, Collections.singletonList( dbMeta ) );
    assertEquals( rowGeneratorMeta.getRowLimit(),  launchVariable );
  }


  @Before
  public void setUpLoadSave() throws Exception {
    HopEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "neverEnding", "intervalInMs", "rowTimeField", "lastTimeField", "rowLimit", "currency", "decimal", "group",
            "value", "fieldName", "fieldType", "fieldFormat", "fieldLength", "fieldPrecision" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "neverEnding", "isNeverEnding" );
        put( "intervalInMs", "getIntervalInMs" );
        put( "rowTimeField", "getRowTimeField" );
        put( "lastTimeField", "getLastTimeField" );
        put( "rowLimit", "getRowLimit" );
        put( "currency", "getCurrency" );
        put( "decimal", "getDecimal" );
        put( "group", "getGroup" );
        put( "value", "getValue" );
        put( "fieldName", "getFieldName" );
        put( "fieldType", "getFieldType" );
        put( "fieldFormat", "getFieldFormat" );
        put( "fieldLength", "getFieldLength" );
        put( "fieldPrecision", "getFieldPrecision" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "neverEnding", "setNeverEnding" );
        put( "intervalInMs", "setIntervalInMs" );
        put( "rowTimeField", "setRowTimeField" );
        put( "lastTimeField", "setLastTimeField" );
        put( "rowLimit", "setRowLimit" );
        put( "currency", "setCurrency" );
        put( "decimal", "setDecimal" );
        put( "group", "setGroup" );
        put( "value", "setValue" );
        put( "fieldName", "setFieldName" );
        put( "fieldType", "setFieldType" );
        put( "fieldFormat", "setFieldFormat" );
        put( "fieldLength", "setFieldLength" );
        put( "fieldPrecision", "setFieldPrecision" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "currency", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "decimal", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "group", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "value", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldType", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormat", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldLength", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 100 ), 5 ) );
    attrValidatorMap.put( "fieldPrecision", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );


    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof RowGeneratorMeta ) {
      ( (RowGeneratorMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws HopException {
    loadSaveTester.testSerialization();
  }
}
