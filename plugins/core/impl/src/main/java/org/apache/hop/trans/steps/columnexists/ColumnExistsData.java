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

package org.apache.hop.trans.steps.columnexists;

import org.apache.hop.core.database.Database;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.trans.step.BaseStepData;
import org.apache.hop.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ColumnExistsData extends BaseStepData implements StepDataInterface {
  public Database db;
  public int indexOfTablename;
  public int indexOfColumnname;
  public String tablename;
  public String schemaname;
  public RowMetaInterface outputRowMeta;

  public ColumnExistsData() {
    super();
    indexOfTablename = -1;
    indexOfColumnname = -1;
    tablename = null;
    schemaname = null;
    db = null;
  }

}
