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

package org.apache.hop.trans.steps.normaliser;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.injection.BaseMetadataInjectionTest;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;

public class NormaliserMetaInjectionTest extends BaseMetadataInjectionTest<NormaliserMeta> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Before
  public void setup() {
    setup( new NormaliserMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "NAME", new StringGetter() {
      public String get() {
        return meta.getNormaliserFields()[0].getName();
      }
    } );
    check( "VALUE", new StringGetter() {
      public String get() {
        return meta.getNormaliserFields()[0].getValue();
      }
    } );
    check( "NORMALISED", new StringGetter() {
      public String get() {
        return meta.getNormaliserFields()[0].getNorm();
      }
    } );
  }

}
