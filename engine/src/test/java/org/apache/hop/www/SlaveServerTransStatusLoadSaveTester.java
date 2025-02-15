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

package org.apache.hop.www;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hop.base.LoadSaveBase;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class SlaveServerTransStatusLoadSaveTester extends LoadSaveBase<SlaveServerTransStatus> {

  public SlaveServerTransStatusLoadSaveTester( Class<SlaveServerTransStatus> clazz, List<String> commonAttributes ) {
    super( clazz, commonAttributes );
  }

  public SlaveServerTransStatusLoadSaveTester( Class<SlaveServerTransStatus> clazz, List<String> commonAttributes,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap ) {
    super( clazz, commonAttributes, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>(),
      new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
      new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public void testSerialization() throws HopException {
    testXmlRoundTrip();
  }

  protected void testXmlRoundTrip() throws HopException {
    SlaveServerTransStatus metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );

    String xml = metaToSave.getXML();
    SlaveServerTransStatus metaLoaded = SlaveServerTransStatus.fromXML( xml );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
