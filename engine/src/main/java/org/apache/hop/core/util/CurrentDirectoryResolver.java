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

package org.apache.hop.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.Const;
import org.apache.hop.core.ObjectLocationSpecificationMethod;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.job.Job;
import org.apache.hop.job.JobMeta;
import org.apache.hop.repository.Repository;
import org.apache.hop.repository.RepositoryDirectoryInterface;
import org.apache.hop.trans.step.StepMeta;

/**
 * This class resolve and update system variables
 * {@link org.apache.hop.core.Const#INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY}
 * {@link org.apache.hop.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY}
 * {@link org.apache.hop.core.Const#INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY}
 * {@link org.apache.hop.core.Const#INTERNAL_VARIABLE_JOB_FILENAME_NAME}
 *
 */
public class CurrentDirectoryResolver {

  public CurrentDirectoryResolver() {
  }

  /**
   * The logic of this method:
   * 
   * if we have directory we return the child var space with directory used as system property
   * if we have not directory we return the child var space with directory extracted from filanme
   * if we don not have directory and filename we will return the child var space without updates
   * 
   * 
   * @param parentVariables - parent variable space which can be inherited
   * @param directory - current path which will be used as path for start trans/job
   * @param filename - is file which we use at this moment
   * @return new var space if inherit was set false or child var space with updated system variables
   */
  public VariableSpace resolveCurrentDirectory( VariableSpace parentVariables, RepositoryDirectoryInterface directory, String filename ) {
    Variables tmpSpace = new Variables();
    tmpSpace.setParentVariableSpace( parentVariables );
    tmpSpace.initializeVariablesFrom( parentVariables );

    if ( directory != null ) {
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, directory.toString() );
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, directory.toString() );
      tmpSpace.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, directory.toString() );
    } else if ( filename != null ) {
      try {
        FileObject fileObject = HopVFS.getFileObject( filename, tmpSpace );

        if ( !fileObject.exists() ) {
          // don't set variables if the file doesn't exist
          return tmpSpace;
        }

        FileName fileName = fileObject.getName();

        // The filename of the transformation
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the transformation
        FileName fileDir = fileName.getParent();
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI() );
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI() );
        tmpSpace.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, fileDir.getURI() );
      } catch ( Exception e ) {
      }
    }
    return tmpSpace;
  }

  public VariableSpace resolveCurrentDirectory( ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, StepMeta stepMeta, String filename ) {
    RepositoryDirectoryInterface directory = null;
    if ( repository != null && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME )
        && stepMeta != null && stepMeta.getParentTransMeta() != null
        && stepMeta.getParentTransMeta().getRepositoryDirectory() != null ) {
      directory = stepMeta.getParentTransMeta().getRepositoryDirectory();
    } else if ( repository == null && stepMeta != null && stepMeta.getParentTransMeta() != null ) {
      filename = stepMeta.getParentTransMeta().getFilename();
    } else if ( stepMeta != null && stepMeta.getParentTransMeta() != null && repository != null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.FILENAME ) ) {
      // we're using FILENAME but we are connected to a repository
      directory = stepMeta.getParentTransMeta().getRepositoryDirectory();
    } else if ( stepMeta != null && stepMeta.getParentTransMeta() != null && filename == null ) {
      filename = stepMeta.getParentTransMeta().getFilename();
    }
    return resolveCurrentDirectory( parentVariables, directory, filename );
  }

  public VariableSpace resolveCurrentDirectory( ObjectLocationSpecificationMethod specificationMethod,
      VariableSpace parentVariables, Repository repository, Job job, String filename ) {
    RepositoryDirectoryInterface directory = null;
    if ( repository != null && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME )
        && job != null && job.getJobMeta() != null && job.getJobMeta().getRepositoryDirectory() != null ) {
      directory = job.getJobMeta().getRepositoryDirectory();
    } else if ( job != null && repository == null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ) ) {
      filename = job.getFilename();
    } else if ( job != null && job.getJobMeta() != null && repository != null
        && specificationMethod.equals( ObjectLocationSpecificationMethod.FILENAME ) ) {
      // we're using FILENAME but we are connected to a repository
      directory = job.getJobMeta().getRepositoryDirectory();
    } else if ( job != null && filename == null ) {
      filename = job.getFilename();
    } else if ( repository != null && JobMeta.class.isAssignableFrom( parentVariables.getClass() ) ) {
      // fallback protection for design mode: the parentVariables may actually be a JobMeta which
      // may have the required directory
      JobMeta realParent = null;
      realParent = (JobMeta) parentVariables;
      if ( realParent != null && realParent.getRepositoryDirectory() != null ) {
        directory = realParent.getRepositoryDirectory();
      }
    } else if ( JobMeta.class.isAssignableFrom( parentVariables.getClass() ) ) {
      // additional fallback protection for design mode
      JobMeta realParent = null;
      realParent = (JobMeta) parentVariables;
      filename = realParent.getFilename();
    }
    return resolveCurrentDirectory( parentVariables, directory, filename );
  }

  public String normalizeSlashes( String str ) {
    if ( StringUtils.isBlank( str ) ) {
      return str;
    }
    while ( str.contains( "\\" ) ) {
      str = str.replace( "\\", "/" );
    }
    while ( str.contains( "//" ) ) {
      str = str.replace( "//", "/" );
    }
    return str;
  }

}
