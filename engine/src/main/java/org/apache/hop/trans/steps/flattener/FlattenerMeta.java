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

package org.apache.hop.trans.steps.flattener;

import java.util.List;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
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

/**
 * The flattener step meta-data
 *
 * @since 17-jan-2006
 * @author Matt
 */

public class FlattenerMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = FlattenerMeta.class; // for i18n purposes, needed by Translator2!!

  /** The field to flatten */
  private String fieldName;

  /** Fields to flatten, same data type as input */
  private String[] targetField;

  public FlattenerMeta() {
    super(); // allocate BaseStepMeta
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  public String[] getTargetField() {
    return targetField;
  }

  public void setTargetField( String[] targetField ) {
    this.targetField = targetField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    targetField = new String[nrfields];
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public void setDefault() {
    int nrfields = 0;

    allocate( nrfields );
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws HopStepException {

    // Remove the key value (there will be different entries for each output row)
    //
    if ( fieldName != null && fieldName.length() > 0 ) {
      int idx = row.indexOfValue( fieldName );
      if ( idx < 0 ) {
        throw new HopStepException( BaseMessages.getString(
          PKG, "FlattenerMeta.Exception.UnableToLocateFieldInInputFields", fieldName ) );
      }

      ValueMetaInterface v = row.getValueMeta( idx );
      row.removeValueMeta( idx );

      for ( int i = 0; i < targetField.length; i++ ) {
        ValueMetaInterface value = v.clone();
        value.setName( targetField[i] );
        value.setOrigin( name );

        row.addValueMeta( value );
      }
    } else {
      throw new HopStepException( BaseMessages.getString( PKG, "FlattenerMeta.Exception.FlattenFieldRequired" ) );
    }
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {
      fieldName = XMLHandler.getTagValue( stepnode, "field_name" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        targetField[i] = XMLHandler.getTagValue( fnode, "name" );
      }
    } catch ( Exception e ) {
      throw new HopXMLException( BaseMessages.getString(
        PKG, "FlattenerMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "      " + XMLHandler.addTagValue( "field_name", fieldName ) );

    retval.append( "      <fields>" + Const.CR );
    for ( int i = 0; i < targetField.length; i++ ) {
      retval.append( "        <field>" + Const.CR );
      retval.append( "          " + XMLHandler.addTagValue( "name", targetField[i] ) );
      retval.append( "          </field>" + Const.CR );
    }
    retval.append( "        </fields>" + Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws HopException {

    try {
      fieldName = rep.getStepAttributeString( id_step, "field_name" );

      int nrvalues = rep.countNrStepAttributes( id_step, "target_field" );

      allocate( nrvalues );

      for ( int i = 0; i < nrvalues; i++ ) {
        targetField[i] = rep.getStepAttributeString( id_step, i, "target_field" );
      }
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString(
        PKG, "FlattenerMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws HopException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "field_name", fieldName );

      for ( int i = 0; i < targetField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "target_field", targetField[i] );
      }
    } catch ( Exception e ) {
      throw new HopException( BaseMessages.getString(
        PKG, "FlattenerMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FlattenerMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FlattenerMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new Flattener( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new FlattenerData();
  }

}
