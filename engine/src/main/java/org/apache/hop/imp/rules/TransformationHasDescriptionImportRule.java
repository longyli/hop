/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.imp.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.imp.rule.ImportRuleInterface;
import org.apache.hop.imp.rule.ImportValidationFeedback;
import org.apache.hop.imp.rule.ImportValidationResultType;
import org.apache.hop.trans.TransMeta;
import org.w3c.dom.Node;

public class TransformationHasDescriptionImportRule extends BaseImportRule implements ImportRuleInterface {

  private int minLength;

  public TransformationHasDescriptionImportRule() {
    super();
    minLength = 20; // Default
  }

  @Override
  public String toString() {
    if ( minLength > 0 ) {
      return super.toString() + " The minimum length is " + minLength;
    } else {
      return super.toString();
    }
  }

  @Override
  public List<ImportValidationFeedback> verifyRule( Object subject ) {

    List<ImportValidationFeedback> feedback = new ArrayList<>();

    if ( !isEnabled() || !( subject instanceof TransMeta ) ) {
      return feedback;
    }

    TransMeta transMeta = (TransMeta) subject;
    String description = transMeta.getDescription();

    if ( null != description && minLength <= description.length() ) {
      feedback.add( new ImportValidationFeedback(
        this, ImportValidationResultType.APPROVAL, "A description is present" ) );
    } else {
      feedback.add( new ImportValidationFeedback(
        this, ImportValidationResultType.ERROR, "A description is not present or is too short." ) );
    }

    return feedback;
  }

  /**
   * @return the minLength
   */
  public int getMinLength() {
    return minLength;
  }

  /**
   * @param minLength
   *          the minLength to set
   */
  public void setMinLength( int minLength ) {
    this.minLength = minLength;
  }

  @Override
  public String getXML() {

    StringBuilder xml = new StringBuilder();
    xml.append( XMLHandler.openTag( XML_TAG ) );

    xml.append( super.getXML() ); // id, enabled

    xml.append( XMLHandler.addTagValue( "min_length", minLength ) );

    xml.append( XMLHandler.closeTag( XML_TAG ) );
    return xml.toString();
  }

  @Override
  public void loadXML( Node ruleNode ) throws HopException {
    super.loadXML( ruleNode );

    minLength = Const.toInt( XMLHandler.getTagValue( ruleNode, "min_length" ), 0 );
  }
}
