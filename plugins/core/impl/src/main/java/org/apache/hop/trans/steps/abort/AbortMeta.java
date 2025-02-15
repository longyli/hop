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

package org.apache.hop.trans.steps.abort;

import java.util.List;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.annotations.Step;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.repository.ObjectId;
import org.apache.hop.repository.Repository;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import static org.apache.hop.core.util.StringUtil.isEmpty;

/**
 * Meta data for the abort step.
 */
@Step( id = "Abort", i18nPackageName = "org.apache.hop.trans.steps.abort",
  name = "Abort.Name", description = "Abort.Description",
  categoryDescription = "i18n:org.apache.hop.trans.step:BaseStep.Category.Flow" )
public class AbortMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = AbortMeta.class; // for i18n purposes, needed by Translator2!!

  public enum AbortOption {
    ABORT,
    ABORT_WITH_ERROR,
    SAFE_STOP
  }

  /**
   * Threshold to abort.
   */
  private String rowThreshold;

  /**
   * Message to put in log when aborting.
   */
  private String message;

  /**
   * Always log rows.
   */
  private boolean alwaysLogRows;

  private AbortOption abortOption;

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws HopStepException {
    // Default: no values are added to the row in the step
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    if ( input.length == 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "AbortMeta.CheckResult.NoInputReceivedError" ), stepinfo );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    return new Abort( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new AbortData();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  public void setDefault() {
    rowThreshold = "0";
    message = "";
    alwaysLogRows = true;
    abortOption = AbortOption.ABORT;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( "      " ).append( XMLHandler.addTagValue( "row_threshold", rowThreshold ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message", message ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "always_log_rows", alwaysLogRows ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "abort_option", abortOption.toString() ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {
      rowThreshold = XMLHandler.getTagValue( stepnode, "row_threshold" );
      message = XMLHandler.getTagValue( stepnode, "message" );
      alwaysLogRows = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "always_log_rows" ) );
      String abortOptionString = XMLHandler.getTagValue( stepnode, "abort_option" );
      if ( !isEmpty( abortOptionString ) ) {
        abortOption = AbortOption.valueOf( abortOptionString );
      } else {
        // Backwards compatibility
        String awe = XMLHandler.getTagValue( stepnode, "abort_with_error" );
        if ( awe == null ) {
          awe = "Y"; // existing transformations will have to maintain backward compatibility with yes
        }
        abortOption = "Y".equalsIgnoreCase( awe ) ? AbortOption.ABORT_WITH_ERROR : AbortOption.ABORT;
      }
    } catch ( Exception e ) {
      throw new HopXMLException( BaseMessages.getString(
        PKG, "AbortMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws HopException {
    try {
      rowThreshold = rep.getStepAttributeString( id_step, "row_threshold" );
      message = rep.getStepAttributeString( id_step, "message" );
      alwaysLogRows = rep.getStepAttributeBoolean( id_step, "always_log_rows" );

      String abortOptionString = rep.getStepAttributeString( id_step, 0, "abort_option" );
      if ( !isEmpty( abortOptionString ) ) {
        abortOption = AbortOption.valueOf( abortOptionString );
      } else {
        // Backward compatibility
        // existing transformations will have to maintain backward compatibility with yes
        boolean abortWithError = rep.getStepAttributeBoolean( id_step, 0, "abort_with_error", true );
        abortOption = abortWithError ? AbortOption.ABORT_WITH_ERROR : AbortOption.ABORT;
      }
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString(
        PKG, "AbortMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws HopException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "row_threshold", rowThreshold );
      rep.saveStepAttribute( id_transformation, id_step, "message", message );
      rep.saveStepAttribute( id_transformation, id_step, "always_log_rows", alwaysLogRows );
      rep.saveStepAttribute( id_transformation, id_step, "abort_option", abortOption.toString() );
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString(
        PKG, "AbortMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getRowThreshold() {
    return rowThreshold;
  }

  public void setRowThreshold( String rowThreshold ) {
    this.rowThreshold = rowThreshold;
  }

  public boolean isAlwaysLogRows() {
    return alwaysLogRows;
  }

  public void setAlwaysLogRows( boolean alwaysLogRows ) {
    this.alwaysLogRows = alwaysLogRows;
  }

  public AbortOption getAbortOption() {
    return abortOption;
  }

  public void setAbortOption( AbortOption abortOption ) {
    this.abortOption = abortOption;
  }

  public boolean isAbortWithError() {
    return abortOption == AbortOption.ABORT_WITH_ERROR;
  }

  public boolean isAbort() {
    return abortOption == AbortOption.ABORT;
  }

  public boolean isSafeStop() {
    return abortOption == AbortOption.SAFE_STOP;
  }
}
