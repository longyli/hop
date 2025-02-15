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

package org.apache.hop.core.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopFileException;
import org.apache.hop.core.util.UUIDUtil;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.vfs.configuration.IHopFileSystemConfigBuilder;
import org.apache.hop.core.vfs.configuration.HopFileSystemConfigBuilderFactory;
import org.apache.hop.core.vfs.configuration.HopGenericFileSystemConfigBuilder;
import org.apache.hop.i18n.BaseMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Comparator;

public class HopVFS {
  public static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );

  private static Class<?> PKG = HopVFS.class; // for i18n purposes, needed by Translator2!!

  private static final HopVFS kettleVFS = new HopVFS();
  private static FileSystemOptions fsOptionsForScheme;
  private final DefaultFileSystemManager fsm;
  private static final int TIMEOUT_LIMIT = 9000;
  private static final int TIME_TO_SLEEP_STEP = 50;

  private static VariableSpace defaultVariableSpace;

  static {
    // Create a new empty variable space...
    //
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
  }

  private HopVFS() {
    fsm = new ConcurrentFileSystemManager();
    try {
      fsm.setFilesCache( new WeakRefFilesCache() );
      fsm.init();
    } catch ( FileSystemException e ) {
      e.printStackTrace();
    }

    // Install a shutdown hook to make sure that the file system manager is closed
    // This will clean up temporary files in vfs_cache
    Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
      @Override
      public void run() {
        if ( fsm != null ) {
          try {
            fsm.close();
          } catch ( Exception ignored ) {
            // Exceptions can be thrown due to a closed classloader
          }
        }
      }
    } ) );
  }

  public FileSystemManager getFileSystemManager() {
    return fsm;
  }

  public static HopVFS getInstance() {
    return kettleVFS;
  }

  public static FileObject getFileObject( String vfsFilename ) throws HopFileException {
    return getFileObject( vfsFilename, defaultVariableSpace );
  }

  public static FileObject getFileObject( String vfsFilename, VariableSpace space ) throws HopFileException {
    return getFileObject( vfsFilename, space, null );
  }

  public static FileObject getFileObject( String vfsFilename, FileSystemOptions fsOptions ) throws HopFileException {
    return getFileObject( vfsFilename, defaultVariableSpace, fsOptions );
  }

  public static FileObject getFileObject( String vfsFilename, VariableSpace space, FileSystemOptions fsOptions ) throws HopFileException {
    try {
      fsOptionsForScheme = fsOptions;
      FileSystemManager fsManager = getInstance().getFileSystemManager();

      // We have one problem with VFS: if the file is in a subdirectory of the current one: somedir/somefile
      // In that case, VFS doesn't parse the file correctly.
      // We need to put file: in front of it to make it work.
      // However, how are we going to verify this?
      //
      // We are going to see if the filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
      // If not, we are going to assume it's a file.
      //
      boolean relativeFilename = true;
      String[] initialSchemes = fsManager.getSchemes();

      relativeFilename = checkForScheme( initialSchemes, relativeFilename, vfsFilename, space, fsOptionsForScheme );

      int timeOut = TIMEOUT_LIMIT;

      boolean hasScheme = vfsFilename != null && vfsFilename.contains( "://" ); // check for bigData providers
      //we have to check for hasScheme even if it is marked as a relative path because that scheme could not
      //be available by getSchemes at the time we validate our relativeFilename flag.
      //So we check if even it is marked as relative path if it contains a possible scheme format
      //if it does, then give it some time to be loaded, until we get it our timeout is up.

      while ( relativeFilename && hasScheme && timeOut > 0 ) {
        String[] schemes = fsManager.getSchemes();
        try {
          Thread.sleep( TIME_TO_SLEEP_STEP );
          timeOut -= TIME_TO_SLEEP_STEP;
          relativeFilename = checkForScheme( schemes, relativeFilename, vfsFilename, space, fsOptionsForScheme );
        } catch ( InterruptedException e ) {
          relativeFilename = false;
          Thread.currentThread().interrupt();
          break;
        }
      }

      String filename;
      if ( vfsFilename.startsWith( "\\\\" ) ) {
        File file = new File( vfsFilename );
        filename = file.toURI().toString();
      } else {
        if ( relativeFilename ) {
          File file = new File( vfsFilename );
          filename = file.getAbsolutePath();
        } else {
          filename = vfsFilename;
        }
      }

      if ( fsOptionsForScheme != null ) {
        return fsManager.resolveFile( filename, fsOptionsForScheme );
      } else {
        return fsManager.resolveFile( filename );
      }
    } catch ( IOException e ) {
      throw new HopFileException( "Unable to get VFS File object for filename '"
        + cleanseFilename( vfsFilename ) + "' : " + e.getMessage(), e );
    }
  }

  protected static boolean checkForScheme( String[] initialSchemes, boolean relativeFilename, String vfsFilename,
                                         VariableSpace space, FileSystemOptions fsOptions )
    throws IOException {
    for ( int i = 0; i < initialSchemes.length && relativeFilename; i++ ) {
      if ( vfsFilename.startsWith( initialSchemes[ i ] + ":" ) ) {
        relativeFilename = false;
        // We have a VFS URL, load any options for the file system driver
        fsOptionsForScheme = buildFsOptions( space, fsOptions, vfsFilename, initialSchemes[ i ] );
      }
    }
    return relativeFilename;
  }

  /**
   * Private method for stripping password from filename when a FileObject
   * can not be obtained.
   * getFriendlyURI(FileObject) or getFriendlyURI(String) are the public
   * methods.
   */
  private static String cleanseFilename( String vfsFilename ) {
    return vfsFilename.replaceAll( ":[^:@/]+@", ":<password>@" );
  }

  private static FileSystemOptions buildFsOptions( VariableSpace varSpace, FileSystemOptions sourceOptions,
      String vfsFilename, String scheme ) throws IOException {
    if ( varSpace == null || vfsFilename == null ) {
      // We cannot extract settings from a non-existant variable space
      return null;
    }

    IHopFileSystemConfigBuilder configBuilder =
        HopFileSystemConfigBuilderFactory.getConfigBuilder( varSpace, scheme );

    FileSystemOptions fsOptions = ( sourceOptions == null ) ? new FileSystemOptions() : sourceOptions;

    String[] varList = varSpace.listVariables();

    for ( String var : varList ) {
      if ( var.startsWith( "vfs." ) ) {
        String param = configBuilder.parseParameterName( var, scheme );
        String varScheme = HopGenericFileSystemConfigBuilder.extractScheme( var );
        if ( param != null ) {
          if ( varScheme == null || varScheme.equals( "sftp" ) || varScheme.equals( scheme ) ) {
            configBuilder.setParameter( fsOptions, param, varSpace.getVariable( var ), var, vfsFilename );
          }
        } else {
          throw new IOException( "FileSystemConfigBuilder could not parse parameter: " + var );
        }
      }
    }
    return fsOptions;
  }

  /**
   * Read a text file (like an XML document). WARNING DO NOT USE FOR DATA FILES.
   *
   * @param vfsFilename
   *          the filename or URL to read from
   * @param charSetName
   *          the character set of the string (UTF-8, ISO8859-1, etc)
   * @return The content of the file as a String
   * @throws IOException
   */
  public static String getTextFileContent( String vfsFilename, String charSetName ) throws HopFileException {
    return getTextFileContent( vfsFilename, null, charSetName );
  }

  public static String getTextFileContent( String vfsFilename, VariableSpace space, String charSetName ) throws HopFileException {
    try {
      InputStream inputStream = null;

      if ( space == null ) {
        inputStream = getInputStream( vfsFilename );
      } else {
        inputStream = getInputStream( vfsFilename, space );
      }
      InputStreamReader reader = new InputStreamReader( inputStream, charSetName );
      int c;
      StringBuilder aBuffer = new StringBuilder();
      while ( ( c = reader.read() ) != -1 ) {
        aBuffer.append( (char) c );
      }
      reader.close();
      inputStream.close();

      return aBuffer.toString();
    } catch ( IOException e ) {
      throw new HopFileException( e );
    }
  }

  public static boolean fileExists( String vfsFilename ) throws HopFileException {
    return fileExists( vfsFilename, null );
  }

  public static boolean fileExists( String vfsFilename, VariableSpace space ) throws HopFileException {
    FileObject fileObject = null;
    try {
      fileObject = getFileObject( vfsFilename, space );
      return fileObject.exists();
    } catch ( IOException e ) {
      throw new HopFileException( e );
    } finally {
      if ( fileObject != null ) {
        try {
          fileObject.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }
  }

  public static InputStream getInputStream( FileObject fileObject ) throws FileSystemException {
    FileContent content = fileObject.getContent();
    return content.getInputStream();
  }

  public static InputStream getInputStream( String vfsFilename ) throws HopFileException {
    return getInputStream( vfsFilename, defaultVariableSpace );
  }

  public static InputStream getInputStream( String vfsFilename, VariableSpace space ) throws HopFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );

      return getInputStream( fileObject );
    } catch ( IOException e ) {
      throw new HopFileException( e );
    }
  }

  public static OutputStream getOutputStream( FileObject fileObject, boolean append ) throws IOException {
    FileObject parent = fileObject.getParent();
    if ( parent != null ) {
      if ( !parent.exists() ) {
        throw new IOException( BaseMessages.getString(
          PKG, "HopVFS.Exception.ParentDirectoryDoesNotExist", getFriendlyURI( parent ) ) );
      }
    }
    try {
      fileObject.createFile();
      FileContent content = fileObject.getContent();
      return content.getOutputStream( append );
    } catch ( FileSystemException e ) {
      // Perhaps if it's a local file, we can retry using the standard
      // File object. This is because on Windows there is a bug in VFS.
      //
      if ( fileObject instanceof LocalFile ) {
        try {
          String filename = getFilename( fileObject );
          return new FileOutputStream( new File( filename ), append );
        } catch ( Exception e2 ) {
          throw e; // throw the original exception: hide the retry.
        }
      } else {
        throw e;
      }
    }
  }

  public static OutputStream getOutputStream( String vfsFilename, boolean append ) throws HopFileException {
    return getOutputStream( vfsFilename, defaultVariableSpace, append );
  }

  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space, boolean append ) throws HopFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new HopFileException( e );
    }
  }

  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space,
      FileSystemOptions fsOptions, boolean append ) throws HopFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space, fsOptions );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new HopFileException( e );
    }
  }

  public static String getFilename( FileObject fileObject ) {
    FileName fileName = fileObject.getName();
    String root = fileName.getRootURI();
    if ( !root.startsWith( "file:" ) ) {
      return fileName.getURI(); // nothing we can do about non-normal files.
    }
    if ( root.startsWith( "file:////" ) ) {
      return fileName.getURI(); // we'll see 4 forward slashes for a windows/smb network share
    }
    if ( root.endsWith( ":/" ) ) { // Windows
      root = root.substring( 8, 10 );
    } else { // *nix & OSX
      root = "";
    }
    String fileString = root + fileName.getPath();
    if ( !"/".equals( Const.FILE_SEPARATOR ) ) {
      fileString = Const.replace( fileString, "/", Const.FILE_SEPARATOR );
    }
    return fileString;
  }

  public static String getFriendlyURI( String filename ) {
    if ( filename == null ) {
      return null;
    }
    String friendlyName;
    try {
      friendlyName = getFriendlyURI( HopVFS.getFileObject( filename ) );
    } catch ( Exception e ) {
      // unable to get a friendly name from VFS object.
      // Cleanse name of pwd before returning
      friendlyName = cleanseFilename( filename );
    }
    return friendlyName;
  }

  public static String getFriendlyURI( FileObject fileObject ) {
    return fileObject.getName().getFriendlyURI();
  }

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix - file name
   * @param prefix - file extension
   * @return FileObject
   * @throws HopFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix ) throws HopFileException {
    return createTempFile( prefix, suffix, TEMP_DIR );
  }

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix        - file name
   * @param suffix        - file extension
   * @param variableSpace is used to get system variables
   * @return FileObject
   * @throws HopFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, VariableSpace variableSpace )
    throws HopFileException {
    return createTempFile( prefix, suffix, TEMP_DIR, variableSpace );
  }

  /**
   *
   * @param prefix    - file name
   * @param suffix    - file extension
   * @param directory - directory where file will be created
   * @return FileObject
   * @throws HopFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory ) throws HopFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  public static FileObject createTempFile( String prefix, String suffix, String directory ) throws HopFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  /**
   * @param prefix    - file name
   * @param directory path to directory where file will be created
   * @param space     is used to get system variables
   * @return FileObject
   * @throws HopFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory, VariableSpace space )
    throws HopFileException {
    return createTempFile( prefix, suffix.ext, directory, space );
  }

  public static FileObject createTempFile( String prefix, String suffix, String directory, VariableSpace space ) throws HopFileException {
    try {
      FileObject fileObject;
      do {
        // Build temporary file name using UUID to ensure uniqueness. Old mechanism would fail using Sort Rows (for
        // example)
        // when there multiple nodes with multiple JVMs on each node. In this case, the temp file names would end up
        // being
        // duplicated which would cause the sort to fail.
        String filename =
            new StringBuilder( 50 ).append( directory ).append( '/' ).append( prefix ).append( '_' ).append(
            UUIDUtil.getUUIDAsString() ).append( suffix ).toString();
        fileObject = getFileObject( filename, space );
      } while ( fileObject.exists() );
      return fileObject;
    } catch ( IOException e ) {
      throw new HopFileException( e );
    }
  }

  public static Comparator<FileObject> getComparator() {
    return new Comparator<FileObject>() {
      @Override
      public int compare( FileObject o1, FileObject o2 ) {
        String filename1 = getFilename( o1 );
        String filename2 = getFilename( o2 );
        return filename1.compareTo( filename2 );
      }
    };
  }

  /**
   * Get a FileInputStream for a local file. Local files can be read with NIO.
   *
   * @param fileObject
   * @return a FileInputStream
   * @throws IOException
   * @deprecated because of API change in Apache VFS. As a workaround use FileObject.getName().getPathDecoded(); Then
   *             use a regular File() object to create a File Input stream.
   */
  @Deprecated
  public static FileInputStream getFileInputStream( FileObject fileObject ) throws IOException {

    if ( !( fileObject instanceof LocalFile ) ) {
      // We can only use NIO on local files at the moment, so that's what we limit ourselves to.
      //
      throw new IOException( BaseMessages.getString( PKG, "FixedInput.Log.OnlyLocalFilesAreSupported" ) );
    }

    return new FileInputStream( fileObject.getName().getPathDecoded() );
  }

  /**
   * Check if filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
   * If yes, return true otherwise return false
   * @param vfsFileName
   * @return boolean
   */
  public static boolean startsWithScheme( String vfsFileName ) {
    FileSystemManager fsManager = getInstance().getFileSystemManager();

    boolean found = false;
    String[] schemes = fsManager.getSchemes();
    for ( int i = 0; i < schemes.length; i++ ) {
      if ( vfsFileName.startsWith( schemes[ i ] + ":" ) ) {
        found = true;
        break;
      }
    }

    return found;
  }

  public static void closeEmbeddedFileSystem( String embeddedMetastoreKey ) {
    if ( getInstance().getFileSystemManager() instanceof ConcurrentFileSystemManager ) {
      ( (ConcurrentFileSystemManager) getInstance().getFileSystemManager() )
        .closeEmbeddedFileSystem( embeddedMetastoreKey );
    }
  }

  public void reset() {
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
    fsm.close();
    try {
      fsm.setFilesCache( new WeakRefFilesCache() );
      fsm.init();
    } catch ( FileSystemException ignored ) {
    }

  }

  /**
   * @see StandardFileSystemManager#freeUnusedResources()
   */
  public static void freeUnusedResources() {
    ( (StandardFileSystemManager) getInstance().getFileSystemManager() ).freeUnusedResources();
  }

  public enum Suffix {
    ZIP( ".zip" ), TMP( ".tmp" ), JAR( ".jar" );

    private String ext;

    Suffix( String ext ) {
      this.ext = ext;
    }
  }

}
