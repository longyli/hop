/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.csvinput;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.QueueRowSet;
import org.apache.hop.core.RowSet;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.steps.StepMockUtil;
import org.apache.hop.trans.steps.mock.StepMockHelper;
import org.apache.hop.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CsvInputMultiCharDelimiterTest extends CsvInputUnitTestBase {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  private CsvInput csvInput;
  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      StepMockUtil.getStepMockHelper( CsvInputMeta.class, "CsvInputMultiCharDelimiterTest" );
    csvInput = new CsvInput(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\r\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNotNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\r\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNotNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3" );
  }

  private void doTest( String content ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( ENCODING, content );
    try {
      CsvInputMeta meta = createMeta( tmp, createInputFileFields( "f1", "f2", "f3" ) );
      CsvInputData data = new CsvInputData();
      csvInput.init( meta, data );

      csvInput.addRowSetToOutputRowSets( output );

      try {
        csvInput.processRow( meta, data );
      } finally {
        csvInput.dispose( meta, data );
      }

    } finally {
      tmp.delete();
    }

    Object[] row = output.getRowImmediate();
    assertNotNull( row );
    assertEquals( "value1", row[ 0 ] );
    assertEquals( "value2", row[ 1 ] );
    assertEquals( "value3", row[ 2 ] );

    assertNull( output.getRowImmediate() );
  }

  @Override
  CsvInputMeta createMeta( File file, TextFileInputField[] fields ) {
    CsvInputMeta meta = super.createMeta( file, fields );
    meta.setDelimiter( "delimiter" );
    return meta;
  }
}
