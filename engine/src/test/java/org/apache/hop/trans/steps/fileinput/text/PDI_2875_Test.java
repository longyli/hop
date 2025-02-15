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

package org.apache.hop.trans.steps.fileinput.text;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.mock.StepMockHelper;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for PDI-2875
 *
 * @author Pavel Sakun
 */
public class PDI_2875_Test {
  private static StepMockHelper<TextFileInputMeta, TextFileInputData> smh;
  private final String VAR_NAME = "VAR";
  private final String EXPRESSION = "${" + VAR_NAME + "}";
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @BeforeClass
  public static void setUp() throws HopException {
    HopEnvironment.init();
    smh =
      new StepMockHelper<TextFileInputMeta, TextFileInputData>( "CsvInputTest", TextFileInputMeta.class, TextFileInputData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void cleanUp() {
    smh.cleanUp();
  }

  private TextFileInputMeta getMeta() {
    TextFileInputMeta meta = new TextFileInputMeta();
    meta.allocateFiles( 2 );
    meta.setFileName( new String[]{ "file1.txt",  "file2.txt" } );
    meta.inputFiles.includeSubFolders = new String[] { "n", "n" };
    meta.setFilter( new TextFileFilter[0] );
    meta.content.fileFormat =  "unix";
    meta.content.fileType = "CSV";
    meta.errorHandling.lineNumberFilesDestinationDirectory = EXPRESSION;
    meta.errorHandling.errorFilesDestinationDirectory =  EXPRESSION;
    meta.errorHandling.warningFilesDestinationDirectory = EXPRESSION;

    return meta;
  }

  @Test
  public void testVariableSubstitution() {
    doReturn( new Date() ).when( smh.trans ).getCurrentDate();
    TextFileInput step = spy( new TextFileInput( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    TextFileInputData data = new TextFileInputData();
    step.setVariable( VAR_NAME, "value" );
    step.init( getMeta(), data );
    verify( step, times( 2 ) ).environmentSubstitute( EXPRESSION );
  }
}
