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

package org.apache.hop.job.entries.createfolder;

import org.apache.hop.job.entry.validator.AbstractFileValidator;
import org.apache.hop.job.entry.validator.AndValidator;
import org.apache.hop.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.hop.cluster.SlaveServer;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Result;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.job.JobMeta;
import org.apache.hop.job.entries.createfile.JobEntryCreateFile;
import org.apache.hop.job.entry.JobEntryBase;
import org.apache.hop.job.entry.JobEntryInterface;
import org.apache.hop.job.entry.validator.ValidatorContext;
import org.apache.hop.repository.ObjectId;
import org.apache.hop.repository.Repository;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'create folder' job entry. Its main use would be to create empty folder that can be used to control
 * the flow in ETL cycles.
 *
 * @author Sven/Samatar
 * @since 18-10-2007
 *
 */
public class JobEntryCreateFolder extends JobEntryBase implements Cloneable, JobEntryInterface {
  private String foldername;
  private boolean failOfFolderExists;

  public JobEntryCreateFolder( String n ) {
    super( n, "" );
    foldername = null;
    failOfFolderExists = true;
  }

  public JobEntryCreateFolder() {
    this( "" );
  }

  public Object clone() {
    JobEntryCreateFolder je = (JobEntryCreateFolder) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "foldername", foldername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fail_of_folder_exists", failOfFolderExists ) );
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( foldername );
    }

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws HopXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      foldername = XMLHandler.getTagValue( entrynode, "foldername" );
      failOfFolderExists = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "fail_of_folder_exists" ) );
    } catch ( HopXMLException xe ) {
      throw new HopXMLException( "Unable to load job entry of type 'create folder' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws HopException {
    try {
      foldername = rep.getJobEntryAttributeString( id_jobentry, "foldername" );
      failOfFolderExists = rep.getJobEntryAttributeBoolean( id_jobentry, "fail_of_folder_exists" );
    } catch ( HopException dbe ) {
      throw new HopException(
        "Unable to load job entry of type 'create Folder' from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws HopException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "foldername", foldername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fail_of_folder_exists", failOfFolderExists );
    } catch ( HopDatabaseException dbe ) {
      throw new HopException( "Unable to save job entry of type 'create Folder' to the repository for id_job="
        + id_job, dbe );
    }
  }

  public void setFoldername( String foldername ) {
    this.foldername = foldername;
  }

  public String getFoldername() {
    return foldername;
  }

  public String getRealFoldername() {
    return environmentSubstitute( getFoldername() );
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    if ( foldername != null ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
        parentJobMeta.getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
      }

      String realFoldername = getRealFoldername();
      FileObject folderObject = null;
      try {
        folderObject = HopVFS.getFileObject( realFoldername, this );

        if ( folderObject.exists() ) {
          boolean isFolder = false;

          // Check if it's a folder
          if ( folderObject.getType() == FileType.FOLDER ) {
            isFolder = true;
          }

          if ( isFailOfFolderExists() ) {
            // Folder exists and fail flag is on.
            result.setResult( false );
            if ( isFolder ) {
              logError( "Folder [" + realFoldername + "] exists, failing." );
            } else {
              logError( "File [" + realFoldername + "] exists, failing." );
            }
          } else {
            // Folder already exists, no reason to try to create it
            result.setResult( true );
            if ( log.isDetailed() ) {
              logDetailed( "Folder [" + realFoldername + "] already exists, not recreating." );
            }
          }

        } else {
          // No Folder yet, create an empty Folder.
          folderObject.createFolder();
          if ( log.isDetailed() ) {
            logDetailed( "Folder [" + realFoldername + "] created!" );
          }
          result.setResult( true );
        }
      } catch ( Exception e ) {
        logError( "Could not create Folder [" + realFoldername + "]", e );
        result.setResult( false );
        result.setNrErrors( 1 );
      } finally {
        if ( folderObject != null ) {
          try {
            folderObject.close();
          } catch ( IOException ex ) { /* Ignore */
          }
        }
      }
    } else {
      logError( "No Foldername is defined." );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isFailOfFolderExists() {
    return failOfFolderExists;
  }

  public void setFailOfFolderExists( boolean failIfFolderExists ) {
    this.failOfFolderExists = failIfFolderExists;
  }

  public static void main( String[] args ) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryCreateFile().check( remarks, null, new Variables(), null, null );
    System.out.printf( "Remarks: %s\n", remarks );
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileDoesNotExistValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );
  }

}
