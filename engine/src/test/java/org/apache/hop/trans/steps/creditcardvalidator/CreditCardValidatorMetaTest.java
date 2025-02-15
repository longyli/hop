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

package org.apache.hop.trans.steps.creditcardvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.trans.steps.loadsave.LoadSaveTester;

public class CreditCardValidatorMetaTest {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Test
  public void testLoadSave() throws HopException {
    List<String> attributes =
      Arrays.asList( "DynamicField", "ResultFieldName", "CardType", "OnlyDigits", "NotValidMsg" );
    LoadSaveTester<CreditCardValidatorMeta> loadSaveTester =
      new LoadSaveTester<CreditCardValidatorMeta>( CreditCardValidatorMeta.class, attributes );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testSupportsErrorHandling() {
    assertTrue( new CreditCardValidatorMeta().supportsErrorHandling() );
  }

  @Test
  public void testDefaults() {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    assertEquals( "result", meta.getResultFieldName() );
    assertFalse( meta.isOnlyDigits() );
    assertEquals( "card type", meta.getCardType() );
    assertEquals( "not valid message", meta.getNotValidMsg() );
  }

  @Test
  public void testGetFields() throws HopStepException {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    meta.setResultFieldName( "The Result Field" );
    meta.setCardType( "The Card Type Field" );
    meta.setNotValidMsg( "Is Card Valid" );

    RowMeta rowMeta = new RowMeta();
    meta.getFields( rowMeta, "this step", null, null, new Variables(), null, null );
    assertEquals( 3, rowMeta.size() );
    assertEquals( "The Result Field", rowMeta.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, rowMeta.getValueMeta( 0 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 0 ).getOrigin() );
    assertEquals( "The Card Type Field", rowMeta.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rowMeta.getValueMeta( 1 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 1 ).getOrigin() );
    assertEquals( "Is Card Valid", rowMeta.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rowMeta.getValueMeta( 2 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 2 ).getOrigin() );
  }
}
