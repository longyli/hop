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

package org.apache.hop.imp.rule;

import java.util.List;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.plugins.ImportRulePluginType;
import org.apache.hop.core.plugins.PluginInterface;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.imp.rules.TransformationHasDescriptionImportRule;
import org.apache.hop.trans.TransMeta;

import junit.framework.TestCase;

public class TransformationHasDescriptionImportRuleIT extends TestCase {

  @Override
  protected void setUp() throws Exception {
    HopEnvironment.init();
  }

  public void testRule() throws Exception {

    TransMeta transMeta = new TransMeta();
    transMeta.setDescription( "This transformation is used for testing an import rule" );

    PluginRegistry registry = PluginRegistry.getInstance();

    PluginInterface plugin =
      registry.findPluginWithId( ImportRulePluginType.class, "TransformationHasDescription" );
    assertNotNull( "The 'transformation has description' rule could not be found in the plugin registry!", plugin );

    TransformationHasDescriptionImportRule rule =
      (TransformationHasDescriptionImportRule) registry.loadClass( plugin );
    assertNotNull(
      "The 'transformation has description rule' class could not be loaded by the plugin registry!", plugin );

    rule.setMinLength( 20 );
    rule.setEnabled( true );

    List<ImportValidationFeedback> feedback = rule.verifyRule( transMeta );
    assertTrue( "We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty() );
    assertTrue(
      "An approval ruling was expected",
      feedback.get( 0 ).getResultType() == ImportValidationResultType.APPROVAL );

    rule.setMinLength( 2000 );
    rule.setEnabled( true );

    feedback = rule.verifyRule( transMeta );
    assertTrue( "We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty() );
    assertTrue(
      "An error ruling was expected", feedback.get( 0 ).getResultType() == ImportValidationResultType.ERROR );

    rule.setEnabled( false );

    feedback = rule.verifyRule( transMeta );
    assertTrue(
      "We didn't expect any feedback from the 'transformation has description rule' while disabled", feedback
        .isEmpty() );

  }
}
