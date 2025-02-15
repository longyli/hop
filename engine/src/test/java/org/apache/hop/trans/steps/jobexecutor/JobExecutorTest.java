/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.jobexecutor;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.job.Job;
import org.apache.hop.job.JobMeta;
import org.apache.hop.repository.Repository;
import org.apache.hop.trans.steps.StepMockUtil;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Mikhail_Chen-Len-Son
 */
public class JobExecutorTest {

  private JobExecutor executor;
  private JobExecutorMeta meta;
  private JobExecutorData data;

  @Before
  public void setUp() throws Exception {
    executor = StepMockUtil.getStep( JobExecutor.class, JobExecutorMeta.class, "TransExecutorUnitTest" );
    executor = spy( executor );
    executor.setInputRowMeta( mock( RowMetaInterface.class ) );

    doNothing().when( executor ).discardLogLines( any( JobExecutorData.class ) );

    meta = new JobExecutorMeta();
    data = new JobExecutorData();
    Job job = mock( Job.class );
    doReturn( job ).when( executor ).createJob( any( Repository.class ), any( JobMeta.class ),
      any( LoggingObjectInterface.class ) );
    doReturn( ArrayUtils.EMPTY_STRING_ARRAY ).when( job ).listParameters();

    data.groupBuffer = new ArrayList<>();
    data.groupSize = -1;
    data.groupTime = -1;
    data.groupField = null;
  }

  @After
  public void tearDown() {
    executor = null;
    meta = null;
    data = null;
  }

  /**
   * Given an input data and a job executor with specified field to group rows on.
   * <br/>
   * When job executor is processing rows of an input data,
   * then rows should be accumulated in a group as long as the specified field value stays the same.
   */
  @Test
  public void shouldAccumulateRowsWhenGroupFieldIsSpecified() throws HopException {
    prepareMultipleRowsForExecutor();

    data.groupField = "groupField";
    executor.init( meta, data );

    when( executor.getExecutorJob() ).thenReturn( mock( Job.class ) );
    when( executor.getExecutorJob().getJobMeta() ).thenReturn( mock( JobMeta.class ) );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "groupField" ) );
    executor.setInputRowMeta( rowMeta );

    // start processing
    executor.processRow( meta, data ); // 1st row - 'value1'
    // should be added to group buffer
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row - still 'value1'
    // first 4 rows should be added to the same group
    assertEquals( 4, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 5th row - value has been changed - 'value12'
    // previous group buffer should be flushed
    // and a new group should be started
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 6th row - 'value12'
    executor.processRow( meta, data ); // 7th row - 'value12'
    // the rest rows should be added to another group
    assertEquals( 3, data.groupBuffer.size() );

    executor.processRow( meta, data ); // end of file
    // group buffer should be flushed in the end
    assertEquals( 0, data.groupBuffer.size() );
  }

  /**
   * Given an input data and a job executor
   * with specified number of rows to send to the transformation (X).
   * <br/>
   * When job executor is processing rows of an input data,
   * then every X rows should be accumulated in a group.
   */
  @Test
  public void shouldAccumulateRowsByCount() throws HopException {
    prepareMultipleRowsForExecutor();

    data.groupSize = 5;
    executor.init( meta, data );

    when( executor.getExecutorJob() ).thenReturn( mock( Job.class ) );
    when( executor.getExecutorJob().getJobMeta() ).thenReturn( mock( JobMeta.class ) );

    // start processing
    executor.processRow( meta, data ); // 1st row
    // should be added to group buffer
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row
    // first 4 rows should be added to the same group
    assertEquals( 4, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 5th row
    // once the 5th row is processed, the transformation executor should be triggered
    // and thus, group buffer should be flushed
    assertEquals( 0, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 6th row
    // previous group buffer should be flushed
    // and a new group should be started
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 7th row
    // the rest rows should be added to another group
    assertEquals( 2, data.groupBuffer.size() );

    executor.processRow( meta, data ); // end of file
    // group buffer should be flushed in the end
    assertEquals( 0, data.groupBuffer.size() );
  }

  // values to be grouped
  private void prepareMultipleRowsForExecutor() throws HopException {
    doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( null )
      .when( executor ).getRow();
  }
}
