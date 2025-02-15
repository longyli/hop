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

package org.apache.hop.trans.steps.prioritizestreams;

import java.util.List;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
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
import org.apache.hop.shared.SharedObjectInterface;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 30-06-2008
 *
 */

public class PrioritizeStreamsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PrioritizeStreamsMeta.class; // for i18n purposes, needed by Translator2!!

  /** by which steps to display? */
  private String[] stepName;

  public PrioritizeStreamsMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    PrioritizeStreamsMeta retval = (PrioritizeStreamsMeta) super.clone();

    int nrfields = stepName.length;

    retval.allocate( nrfields );
    System.arraycopy( stepName, 0, retval.stepName, 0, nrfields );
    return retval;
  }

  public void allocate( int nrfields ) {
    stepName = new String[nrfields];
  }

  /**
   * @return Returns the stepName.
   */
  public String[] getStepName() {
    return stepName;
  }

  /**
   * @param stepName
   *          The stepName to set.
   */
  public void setStepName( String[] stepName ) {
    this.stepName = stepName;
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws HopStepException {
    // Default: nothing changes to rowMeta
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws HopXMLException {
    try {
      Node steps = XMLHandler.getSubNode( stepnode, "steps" );
      int nrsteps = XMLHandler.countNodes( steps, "step" );

      allocate( nrsteps );

      for ( int i = 0; i < nrsteps; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( steps, "step", i );
        stepName[i] = XMLHandler.getTagValue( fnode, "name" );
      }
    } catch ( Exception e ) {
      throw new HopXMLException( "Unable to load step info from XML", e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    <steps>" + Const.CR );
    for ( int i = 0; i < stepName.length; i++ ) {
      retval.append( "      <step>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", stepName[i] ) );
      retval.append( "        </step>" + Const.CR );
    }
    retval.append( "      </steps>" + Const.CR );

    return retval.toString();
  }

  public void setDefault() {
    int nrsteps = 0;

    allocate( nrsteps );

    for ( int i = 0; i < nrsteps; i++ ) {
      stepName[i] = "step" + i;
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws HopException {
    try {

      int nrsteps = rep.countNrStepAttributes( id_step, "step_name" );

      allocate( nrsteps );

      for ( int i = 0; i < nrsteps; i++ ) {
        stepName[i] = rep.getStepAttributeString( id_step, i, "step_name" );
      }
    } catch ( Exception e ) {
      throw new HopException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws HopException {
    try {
      for ( int i = 0; i < stepName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "step_name", stepName[i] );
      }
    } catch ( Exception e ) {
      throw new HopException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "PrioritizeStreamsMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      if ( stepName.length > 0 ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "PrioritizeStreamsMeta.CheckResult.AllStepsFound" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "PrioritizeStreamsMeta.CheckResult.NoStepsEntered" ), stepMeta );
        remarks.add( cr );
      }

    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "PrioritizeStreamsMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "PrioritizeStreamsMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new PrioritizeStreams( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new PrioritizeStreamsData();
  }

}
