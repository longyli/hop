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
package org.apache.hop.trans.steps.loadsave.validator;

import java.util.Random;
import java.util.UUID;

import org.apache.hop.trans.steps.mapping.MappingParameters;

public class MappingParametersLoadSaveValidator implements FieldLoadSaveValidator<MappingParameters> {
  final Random rand = new Random();
  @Override
  public MappingParameters getTestObject() {
    MappingParameters rtn = new MappingParameters();
    rtn.setVariable( new String[] {
      UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()
    } );
    rtn.setInputField( new String[] {
      UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()
    } );
    rtn.setInheritingAllVariables( rand.nextBoolean() );
    return rtn;
  }

  @Override
  public boolean validateTestObject( MappingParameters testObject, Object actual ) {
    if ( !( actual instanceof MappingParameters ) ) {
      return false;
    }
    MappingParameters actualInput = (MappingParameters) actual;
    return ( testObject.getXML().equals( actualInput.getXML() ) );
  }
}
