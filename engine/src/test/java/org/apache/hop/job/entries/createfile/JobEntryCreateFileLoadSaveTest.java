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
package org.apache.hop.job.entries.createfile;

import org.junit.ClassRule;
import org.apache.hop.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobEntryCreateFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCreateFile> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();
  @Override protected Class<JobEntryCreateFile> getJobEntryClass() {
    return JobEntryCreateFile.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "filename", "failIfFileExists", "addfilenameresult" );
  }

  @Override protected Map<String, String> createGettersMap() {
    return Collections.singletonMap( "addfilenameresult", "isAddFilenameToResult" );
  }

  @Override protected Map<String, String> createSettersMap() {
    return Collections.singletonMap( "addfilenameresult", "setAddFilenameToResult" );
  }
}
