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

package org.apache.hop.core.util;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.util.PluginPropertyHandler.LoadXml;
import org.apache.hop.core.util.PluginPropertyHandler.ReadFromPreferences;
import org.apache.hop.core.util.PluginPropertyHandler.ReadFromRepository;
import org.apache.hop.core.util.PluginPropertyHandler.SaveToPreferences;
import org.apache.hop.core.util.PluginPropertyHandler.SaveToRepository;
import org.apache.hop.repository.ObjectId;
import org.apache.hop.repository.Repository;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepMetaInterface;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
 *
 */
/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 *
 */
public abstract class AbstractStepMeta extends BaseStepMeta implements StepMetaInterface {

  private static final String CONNECTION_NAME = "connection";

  private final PluginPropertyFactory propertyFactory = new PluginPropertyFactory( new KeyValueSet() );

  private DatabaseMeta dbMeta;

  private StringPluginProperty connectionName;

  /**
   * Default constructor.
   */
  public AbstractStepMeta() {
    super();
    this.connectionName = this.propertyFactory.createString( CONNECTION_NAME );
  }

  /**
   * @return the propertyFactory
   */
  public PluginPropertyFactory getPropertyFactory() {
    return this.propertyFactory;
  }

  /**
   * @return the properties
   */
  public KeyValueSet getProperties() {
    return this.propertyFactory.getProperties();
  }

  /**
   * Saves properties to preferences.
   *
   * @throws BackingStoreException
   *           ...
   */
  public void saveAsPreferences() throws BackingStoreException {
    final Preferences node = Preferences.userNodeForPackage( this.getClass() );
    this.getProperties().walk( new SaveToPreferences( node ) );
    node.flush();
  }

  /**
   * Read properties from preferences.
   */
  public void readFromPreferences() {
    final Preferences node = Preferences.userNodeForPackage( this.getClass() );
    this.getProperties().walk( new ReadFromPreferences( node ) );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.hop.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
   */
  public void loadXML( final Node node, final List<DatabaseMeta> databaseMeta, final IMetaStore metaStore ) throws HopXMLException {
    this.getProperties().walk( new LoadXml( node ) );
    initDbMeta( databaseMeta );
  }

  /**
   * @param databaseList
   *          A list of available DatabaseMeta in this transformation.
   */
  private void initDbMeta( final List<DatabaseMeta> databaseList ) {
    if ( !StringUtils.isEmpty( this.connectionName.getValue() ) ) {
      this.dbMeta = DatabaseMeta.findDatabase( databaseList, this.connectionName.getValue() );
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.hop.trans.step.BaseStepMeta#getXML()
   */
  @Override
  public String getXML() throws HopException {
    return PluginPropertyHandler.toXml( this.getProperties() );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.hop.trans.step.StepMetaInterface#readRep(org.apache.hop.repository.Repository, long,
   *      java.util.List, java.util.Map)
   */
  public void readRep( final Repository repo, final IMetaStore metaStore, final ObjectId stepId,
    final List<DatabaseMeta> databaseList ) throws HopException {
    PluginPropertyHandler.walk( this.getProperties(), new ReadFromRepository( repo, metaStore, stepId ) );
    initDbMeta( databaseList );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.hop.trans.step.StepMetaInterface#saveRep(org.apache.hop.repository.Repository, long, long)
   */
  public void saveRep( final Repository repo, final IMetaStore metaStore, final ObjectId transformationId,
    final ObjectId stepId ) throws HopException {
    final SaveToRepository handler = new SaveToRepository( repo, metaStore, transformationId, stepId );
    PluginPropertyHandler.walk( this.getProperties(), handler );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.apache.hop.trans.step.StepMetaInterface#getStepData()
   */
  public StepDataInterface getStepData() {
    // you may be override this.
    return new GenericStepData();
  }

  /**
   * @return the connectionName
   */
  public StringPluginProperty getConnectionName() {
    return this.connectionName;
  }

  /**
   * @param connectionName
   *          the connectionName to set
   */
  public void setConnectionName( final StringPluginProperty connectionName ) {
    this.connectionName = connectionName;
  }

  /**
   * @return the dbMeta
   */
  public DatabaseMeta getDbMeta() {
    return this.dbMeta;
  }

  /**
   * @param dbMeta
   *          the dbMeta to set
   */
  public void setDbMeta( final DatabaseMeta dbMeta ) {
    this.dbMeta = dbMeta;
  }
}
