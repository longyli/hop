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

package org.apache.hop.trans.steps.cubeoutput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;
import org.apache.hop.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class CubeOutputMetaTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();
  @Test
  public void testRoundTrip() throws HopException {
    List<String> attributes =
      Arrays.asList( "name", "add_to_result_filenames", "do_not_open_newfile_init" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "name", "getFilename" );
    getterMap.put( "add_to_result_filenames", "isAddToResultFiles" );
    getterMap.put( "do_not_open_newfile_init", "isDoNotOpenNewFileInit" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "name", "setFilename" );
    setterMap.put( "add_to_result_filenames", "setAddToResultFiles" );
    setterMap.put( "do_not_open_newfile_init", "setDoNotOpenNewFileInit" );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( CubeOutputMeta.class, attributes, getterMap, setterMap,
          new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
