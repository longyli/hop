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

package org.apache.hop.trans.steps.multimerge;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.injection.Injection;
import org.apache.hop.core.injection.InjectionSupported;
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
import org.apache.hop.trans.step.StepIOMetaInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.trans.step.errorhandling.Stream;
import org.apache.hop.trans.step.errorhandling.StreamIcon;
import org.apache.hop.trans.step.errorhandling.StreamInterface.StreamType;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Biswapesh
 * @since 24-nov-2006
 */
@InjectionSupported( localizationPrefix = "MultiMergeJoin.Injection." )
public class MultiMergeJoinMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MultiMergeJoinMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] join_types = { "INNER", "FULL OUTER" };
  public static final boolean[] optionals = { false, true };

  @Injection( name = "JOIN_TYPE" )
  private String joinType;

  /**
   * comma separated key values for each stream
   */
  @Injection( name = "KEY_FIELDS" )
  private String[] keyFields;

  /**
   * input stream names
   */
  @Injection( name = "INPUT_STEPS" )
  private String[] inputSteps;

  /**
   * The supported join types are INNER, LEFT OUTER, RIGHT OUTER and FULL OUTER
   *
   * @return The type of join
   */
  public String getJoinType() {
    return joinType;
  }

  /**
   * Sets the type of join
   *
   * @param joinType
   *          The type of join, e.g. INNER/FULL OUTER
   */
  public void setJoinType( String joinType ) {
    this.joinType = joinType;
  }

  /**
   * @return Returns the keyFields1.
   */
  public String[] getKeyFields() {
    return keyFields;
  }

  /**
   * @param keyFields1
   *          The keyFields1 to set.
   */
  public void setKeyFields( String[] keyFields ) {
    this.keyFields = keyFields;
  }

  @Override
  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  public MultiMergeJoinMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  public void allocateKeys( int nrKeys ) {
    keyFields = new String[nrKeys];
  }

  @Override
  public Object clone() {
    MultiMergeJoinMeta retval = (MultiMergeJoinMeta) super.clone();
    int nrKeys = keyFields == null ? 0 : keyFields.length;
    int nrSteps = inputSteps == null ? 0 : inputSteps.length;
    retval.allocateKeys( nrKeys );
    retval.allocateInputSteps( nrSteps );
    System.arraycopy( keyFields, 0, retval.keyFields, 0, nrKeys );
    System.arraycopy( inputSteps, 0, retval.inputSteps, 0, nrSteps );
    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    String[] inputStepsNames  = inputSteps != null ? inputSteps : ArrayUtils.EMPTY_STRING_ARRAY;
    retval.append( "    " ).append( XMLHandler.addTagValue( "join_type", getJoinType() ) );
    for ( int i = 0; i < inputStepsNames.length; i++ ) {
      retval.append( "    " ).append( XMLHandler.addTagValue( "step" + i, inputStepsNames[ i ] ) );
    }

    retval.append( "    " ).append( XMLHandler.addTagValue( "number_input", inputStepsNames.length ) );
    retval.append( "    " ).append( XMLHandler.openTag( "keys" ) ).append( Const.CR );
    for ( int i = 0; i < keyFields.length; i++ ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "key", keyFields[i] ) );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( "keys" ) ).append( Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {

      Node keysNode = XMLHandler.getSubNode( stepnode, "keys" );

      int nrKeys = XMLHandler.countNodes( keysNode, "key" );

      allocateKeys( nrKeys );

      for ( int i = 0; i < nrKeys; i++ ) {
        Node keynode = XMLHandler.getSubNodeByNr( keysNode, "key", i );
        keyFields[i] = XMLHandler.getNodeValue( keynode );
      }

      int nInputStreams = Integer.parseInt( XMLHandler.getTagValue( stepnode, "number_input" ) );

      allocateInputSteps( nInputStreams );

      for ( int i = 0; i < nInputStreams; i++ ) {
        inputSteps[i] = XMLHandler.getTagValue( stepnode, "step" + i );
      }

      joinType = XMLHandler.getTagValue( stepnode, "join_type" );
    } catch ( Exception e ) {
      throw new HopXMLException( BaseMessages.getString( PKG, "MultiMergeJoinMeta.Exception.UnableToLoadStepInfo" ),
          e );
    }
  }

  @Override
  public void setDefault() {
    joinType = join_types[0];
    allocateKeys( 0 );
    allocateInputSteps( 0 );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws HopException {
    try {
      int nrKeys = rep.countNrStepAttributes( id_step, "keys" );

      allocateKeys( nrKeys );

      for ( int i = 0; i < nrKeys; i++ ) {
        keyFields[i] = rep.getStepAttributeString( id_step, i, "keys" );
      }

      long nInputStreams = rep.getStepAttributeInteger( id_step, "number_input" );

      allocateInputSteps( (int) nInputStreams );

      for ( int i = 0; i < nInputStreams; i++ ) {
        inputSteps[i] = rep.getStepAttributeString( id_step, "step" + i );
      }
      // This next bit is completely unnecessary if you just pass the step name into
      // the constructor above. That sets the subject to the step name in one pass
      // instead of a second one.
      // MB - 5/2016
      //
      // List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
      // for ( int i = 0; i < infoStreams.size(); i++ ) {
      //   infoStreams.get( i ).setSubject( rep.getStepAttributeString( id_step, "step" + i ) );
      // }

      joinType = rep.getStepAttributeString( id_step, "join_type" );
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString( PKG,
          "MultiMergeJoinMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    StepIOMetaInterface ioMeta = getStepIOMeta();
    ioMeta.getInfoStreams().clear();
    for ( int i = 0; i < inputSteps.length; i++ ) {
      String inputStepName = inputSteps[i];
      if ( i >= ioMeta.getInfoStreams().size() ) {
        ioMeta.addStream(
          new Stream( StreamType.INFO, StepMeta.findStep( steps, inputStepName ),
              BaseMessages.getString( PKG, "MultiMergeJoin.InfoStream.Description" ), StreamIcon.INFO, inputStepName ) );
      }
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws HopException {
    try {
      for ( int i = 0; i < keyFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "keys", keyFields[i] );
      }

      String[] inputStepsNames  = inputSteps != null ? inputSteps : ArrayUtils.EMPTY_STRING_ARRAY;
      rep.saveStepAttribute( id_transformation, id_step, "number_input", inputStepsNames.length );
      for ( int i = 0; i < inputStepsNames.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, "step" + i, inputStepsNames[ i ] );
      }
//      The following was the old way of persisting this step to the repository. This was inconsistent with
//      how getXML works, and also fails the load/save tester
//      List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
//      rep.saveStepAttribute( id_transformation, id_step, "number_input", infoStreams.size() );
//      for ( int i = 0; i < infoStreams.size(); i++ ) {
//        rep.saveStepAttribute( id_transformation, id_step, "step" + i, infoStreams.get( i ).getStepname() );
//      }
      // inputSteps[i]
      rep.saveStepAttribute( id_transformation, id_step, "join_type", getJoinType() );
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString( PKG, "MultiMergeJoinMeta.Exception.UnableToSaveStepInfo" )
          + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    /*
     * @todo Need to check for the following: 1) Join type must be one of INNER / LEFT OUTER / RIGHT OUTER / FULL OUTER
     * 2) Number of input streams must be two (for now at least) 3) The field names of input streams must be unique
     */
    CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString( PKG,
            "MultiMergeJoinMeta.CheckResult.StepNotVerified" ), stepMeta );
    remarks.add( cr );
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws HopStepException {
    // We don't have any input fields here in "r" as they are all info fields.
    // So we just merge in the info fields.
    //
    if ( info != null ) {
      for ( int i = 0; i < info.length; i++ ) {
        if ( info[i] != null ) {
          r.mergeRowMeta( info[i] );
        }
      }
    }

    for ( int i = 0; i < r.size(); i++ ) {
      r.getValueMeta( i ).setOrigin( name );
    }
    return;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new MultiMergeJoin( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MultiMergeJoinData();
  }

  @Override
  public void resetStepIoMeta() {
    // Don't reset!
  }

  public void setInputSteps( String[] inputSteps ) {
    this.inputSteps = inputSteps;
  }

  public String[] getInputSteps() {
    return inputSteps;
  }

  public void allocateInputSteps( int count ) {
    inputSteps = new String[count];

  }
}
