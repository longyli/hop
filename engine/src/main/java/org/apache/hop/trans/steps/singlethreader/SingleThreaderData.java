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

package org.apache.hop.trans.steps.singlethreader;

import java.util.List;

import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.trans.RowProducer;
import org.apache.hop.trans.SingleThreadedTransExecutor;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepData;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepMeta;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class SingleThreaderData extends BaseStepData implements StepDataInterface {
  public Trans mappingTrans;

  public SingleThreadedTransExecutor executor;

  public int batchSize;

  public TransMeta mappingTransMeta;

  public RowMetaInterface outputRowMeta;
  public RowProducer rowProducer;

  public int batchCount;
  public int batchTime;
  public long startTime;
  public StepMeta injectStepMeta;
  public StepMeta retrieveStepMeta;
  public List<Object[]> errorBuffer;
  public int lastLogLine;

  public SingleThreaderData() {
    super();
    mappingTrans = null;

  }

  public Trans getMappingTrans() {
    return mappingTrans;
  }

  public void setMappingTrans( Trans mappingTrans ) {
    this.mappingTrans = mappingTrans;
  }
}
