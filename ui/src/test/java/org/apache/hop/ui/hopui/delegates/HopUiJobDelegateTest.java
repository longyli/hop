/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.apache.hop.ui.hopui.delegates;


import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.logging.JobLogTable;
import org.apache.hop.core.logging.LogChannelInterface;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.plugins.ClassLoadingPluginInterface;
import org.apache.hop.core.plugins.JobEntryPluginType;
import org.apache.hop.core.plugins.PluginInterface;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.job.JobExecutionConfiguration;
import org.apache.hop.job.JobMeta;
import org.apache.hop.job.entry.JobEntryDialogInterface;
import org.apache.hop.job.entry.JobEntryInterface;
import org.apache.hop.ui.job.dialog.JobExecutionConfigurationDialog;
import org.apache.hop.ui.hopui.HopUi;
import org.apache.hop.ui.hopui.job.JobGraph;
import org.apache.hop.ui.hopui.job.JobLogDelegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HopUiJobDelegateTest {
  private static final String[] EMPTY_STRING_ARRAY = new String[]{};
  private static final String TEST_VARIABLE_KEY = "variableKey";
  private static final String TEST_VARIABLE_VALUE = "variableValue";
  private static final Map<String, String> MAP_WITH_TEST_VARIABLE = new HashMap<String, String>() {
    {
      put( TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE );
    }
  };
  private static final String TEST_PARAM_KEY = "paramKey";
  private static final String TEST_PARAM_VALUE = "paramValue";
  private static final Map<String, String> MAP_WITH_TEST_PARAM = new HashMap<String, String>() {
    {
      put( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    }
  };
  private static final LogLevel TEST_LOG_LEVEL = LogLevel.BASIC;
  private static final String TEST_START_COPY_NAME = "startCopyName";
  private static final boolean TEST_BOOLEAN_PARAM = true;

  private HopUiJobDelegate delegate;
  private HopUi hopUi;
  private JobLogTable jobLogTable;
  private JobMeta jobMeta;
  private ArrayList<JobMeta> jobMap;

  @Before
  public void before() {
    jobMap = new ArrayList<JobMeta>();

    jobMeta = mock( JobMeta.class );
    delegate = mock( HopUiJobDelegate.class );
    hopUi = mock( HopUi.class );
    hopUi.delegates = mock( HopUiDelegates.class );
    hopUi.delegates.tabs = mock( HopUiTabsDelegate.class );
    hopUi.variables = mock( RowMetaAndData.class );
    delegate.hopUi = hopUi;

    doReturn( jobMap ).when( delegate ).getJobList();
    doReturn( hopUi ).when( delegate ).getSpoon();
    jobLogTable = mock( JobLogTable.class );
  }

  @Test
  public void testAddAndCloseTransformation() {
    doCallRealMethod().when( delegate ).closeJob( any() );
    doCallRealMethod().when( delegate ).addJob( any() );
    assertTrue( delegate.addJob( jobMeta ) );
    assertFalse( delegate.addJob( jobMeta ) );
    delegate.closeJob( jobMeta );
    assertTrue( delegate.addJob( jobMeta ) );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testSetParamsIntoMetaInExecuteJob() throws HopException {
    doCallRealMethod().when( delegate ).executeJob( jobMeta, true, false, null, false,
        null, 0 );

    JobExecutionConfiguration jobExecutionConfiguration = mock( JobExecutionConfiguration.class );
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    Shell shell = mock( Shell.class );
    JobExecutionConfigurationDialog jobExecutionConfigurationDialog = mock( JobExecutionConfigurationDialog.class );
    JobGraph activeJobGraph = mock( JobGraph.class );
    activeJobGraph.jobLogDelegate = mock( JobLogDelegate.class );


    doReturn( jobExecutionConfiguration ).when( hopUi ).getJobExecutionConfiguration();
    doReturn( rowMeta ).when( hopUi.variables ).getRowMeta();
    doReturn( EMPTY_STRING_ARRAY ).when( rowMeta ).getFieldNames();
    doReturn( shell ).when( hopUi ).getShell();
    doReturn( jobExecutionConfigurationDialog ).when( delegate )
      .newJobExecutionConfigurationDialog( jobExecutionConfiguration, jobMeta );
    doReturn( activeJobGraph ).when( hopUi ).getActiveJobGraph();
    doReturn( MAP_WITH_TEST_VARIABLE ).when( jobExecutionConfiguration ).getVariables();
    doReturn( MAP_WITH_TEST_PARAM ).when( jobExecutionConfiguration ).getParams();
    doReturn( TEST_LOG_LEVEL ).when( jobExecutionConfiguration ).getLogLevel();
    doReturn( TEST_START_COPY_NAME ).when( jobExecutionConfiguration ).getStartCopyName();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isClearingLog();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isSafeModeEnabled();
    doReturn( TEST_BOOLEAN_PARAM ).when( jobExecutionConfiguration ).isExpandingRemoteJob();

    delegate.executeJob( jobMeta, true, false, null, false,
        null, 0 );

    verify( jobMeta ).setVariable( TEST_VARIABLE_KEY, TEST_VARIABLE_VALUE );
    verify( jobMeta ).setParameterValue( TEST_PARAM_KEY, TEST_PARAM_VALUE );
    verify( jobMeta ).activateParameters();
    verify( jobMeta ).setLogLevel( TEST_LOG_LEVEL );
    verify( jobMeta ).setStartCopyName( TEST_START_COPY_NAME );
    verify( jobMeta ).setClearingLog( TEST_BOOLEAN_PARAM );
    verify( jobMeta ).setSafeModeEnabled( TEST_BOOLEAN_PARAM );
    verify( jobMeta ).setExpandingRemoteJob( TEST_BOOLEAN_PARAM );
  }

  @Test
  public void testGetJobEntryDialogClass() throws HopPluginException {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginMockInterface plugin = mock( PluginMockInterface.class );
    when( plugin.getIds() ).thenReturn( new String[] { "mockJobPlugin" } );
    when( plugin.matches( "mockJobPlugin" ) ).thenReturn( true );
    when( plugin.getName() ).thenReturn( "mockJobPlugin" );

    JobEntryInterface jobEntryInterface = mock( JobEntryInterface.class );
    when( jobEntryInterface.getDialogClassName() ).thenReturn( String.class.getName() );
    when( plugin.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( JobEntryInterface.class, jobEntryInterface.getClass().getName() );
        put( JobEntryDialogInterface.class, JobEntryDialogInterface.class.getName() );
      }} );

    registry.registerPlugin( JobEntryPluginType.class, plugin );

    HopUiJobDelegate delegate = mock( HopUiJobDelegate.class );
    HopUi hopUi = mock( HopUi.class );
    delegate.hopUi = hopUi;
    delegate.log = mock( LogChannelInterface.class );
    when( hopUi.getShell() ).thenReturn( mock( Shell.class ) );
    doCallRealMethod().when( delegate ).getJobEntryDialog( any( JobEntryInterface.class ), any( JobMeta.class ) );

    JobMeta meta = mock( JobMeta.class );

    // verify that dialog class is requested from plugin
    try {
      delegate.getJobEntryDialog( jobEntryInterface, meta ); // exception is expected here
    } catch ( Throwable ignore ) {
      verify( jobEntryInterface, never() ).getDialogClassName();
    }

    // verify that the deprecated way is still valid
    when( plugin.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( JobEntryInterface.class, jobEntryInterface.getClass().getName() );
      }} );
    try {
      delegate.getJobEntryDialog( jobEntryInterface, meta ); // exception is expected here
    } catch ( Throwable ignore ) {
      verify( jobEntryInterface, times( 1 ) ).getDialogClassName();
    }

    // cleanup
    registry.removePlugin( JobEntryPluginType.class, plugin );
  }

  public interface PluginMockInterface extends ClassLoadingPluginInterface, PluginInterface {
  }
}
