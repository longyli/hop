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

package org.apache.hop.trans.steps.jobexecutor;

import java.util.List;

import org.apache.hop.core.RowMetaAndData;
import org.apache.hop.core.RowSet;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.job.Job;
import org.apache.hop.job.JobMeta;
import org.apache.hop.trans.step.BaseStepData;
import org.apache.hop.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JobExecutorData extends BaseStepData implements StepDataInterface {
  public Job executorJob;
  public JobMeta executorJobMeta;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface executionResultsOutputRowMeta;
  public RowMetaInterface resultRowsOutputRowMeta;
  public RowMetaInterface resultFilesOutputRowMeta;

  public List<RowMetaAndData> groupBuffer;
  public int groupSize;
  public int groupTime;
  public long groupTimeStart;
  public String groupField;
  public int groupFieldIndex;
  public ValueMetaInterface groupFieldMeta;
  public Object prevGroupFieldData;
  public RowSet resultRowsRowSet;
  public RowSet resultFilesRowSet;
  public RowSet executionResultRowSet;

  public JobExecutorData() {
    super();
  }

}
