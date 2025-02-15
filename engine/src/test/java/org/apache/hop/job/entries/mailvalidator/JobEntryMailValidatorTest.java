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
package org.apache.hop.job.entries.mailvalidator;

import static org.junit.Assert.assertNotNull;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.Result;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;


public class JobEntryMailValidatorTest extends JobEntryLoadSaveTestSupport<JobEntryMailValidator> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Override
  protected Class<JobEntryMailValidator> getJobEntryClass() {
    return JobEntryMailValidator.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "smtpCheck",
        "timeout",
        "defaultSMTP",
        "emailSender",
        "emailAddress" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "smtpCheck", "isSMTPCheck",
        "timeout", "getTimeOut",
        "defaultSMTP", "getDefaultSMTP",
        "emailSender", "geteMailSender",
        "emailAddress", "getEmailAddress" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "smtpCheck", "setSMTPCheck",
        "timeout", "setTimeOut",
        "defaultSMTP", "setDefaultSMTP",
        "emailSender", "seteMailSender",
        "emailAddress", "setEmailAddress" );
  }

  @Test
  public void testExecute() {
    HopLogStore.init();
    Result previousResult = new Result();
    JobEntryMailValidator validator = new JobEntryMailValidator();
    Result result = validator.execute( previousResult, 0 );
    assertNotNull( result );
  }

}
