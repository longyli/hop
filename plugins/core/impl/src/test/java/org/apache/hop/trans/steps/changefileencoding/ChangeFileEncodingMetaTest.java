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

package org.apache.hop.trans.steps.changefileencoding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;

public class ChangeFileEncodingMetaTest {
  @ClassRule
  public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Test
  public void testRoundTrip() throws HopException {
    List<String> attributes =
        Arrays.asList( "filenamefield", "targetfilenamefield", "sourceencoding", "targetencoding",
          "addsourceresultfilenames", "addtargetresultfilenames", "createparentfolder" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "filenamefield", "getDynamicFilenameField" );
    getterMap.put( "targetfilenamefield", "getTargetFilenameField" );
    getterMap.put( "sourceencoding", "getSourceEncoding" );
    getterMap.put( "targetencoding", "getTargetEncoding" );
    getterMap.put( "addsourceresultfilenames", "addSourceResultFilenames" );
    getterMap.put( "addtargetresultfilenames", "addTargetResultFilenames" );
    getterMap.put( "createparentfolder", "isCreateParentFolder" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "filenamefield", "setDynamicFilenameField" );
    setterMap.put( "targetfilenamefield", "setTargetFilenameField" );
    setterMap.put( "sourceencoding", "setSourceEncoding" );
    setterMap.put( "targetencoding", "setTargetEncoding" );
    setterMap.put( "addsourceresultfilenames", "setaddSourceResultFilenames" );
    setterMap.put( "addtargetresultfilenames", "setaddTargetResultFilenames" );
    setterMap.put( "createparentfolder", "setCreateParentFolder" );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( ChangeFileEncodingMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
