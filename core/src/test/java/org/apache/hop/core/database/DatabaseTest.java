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

package org.apache.hop.core.database;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.apache.hop.junit.rules.RestoreHopEnvironment;
import org.junit.*;
import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.database.DataSourceProviderInterface.DatasourceType;
import org.apache.hop.core.exception.HopDatabaseBatchException;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.variables.VariableSpace;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

@SuppressWarnings( "deprecation" )
public class DatabaseTest {

  @ClassRule
  public static RestoreHopEnvironment env = new RestoreHopEnvironment();

  private static final String TEST_NAME_OF_DB_CONNECTION = "TEST_CONNECTION";
  private static final String SQL_MOCK_EXCEPTION_MESSAGE = "SQL mock exception";
  private static final SQLException SQL_EXCEPTION = new SQLException( SQL_MOCK_EXCEPTION_MESSAGE );
  private static final String EXISTING_TABLE_NAME = "TABLE";
  private static final String NOT_EXISTING_TABLE_NAME = "NOT_EXISTING_TABLE";
  private static final String SCHEMA_TO_CHECK = "schemaPattern";
  private static final String[] TABLE_TYPES_TO_GET = { "TABLE", "VIEW" };

  //common fields
  private String sql = "select * from employees";
  private String columnName = "salary";
  private String fullJndiName = "jdbc/testJNDIName";
  private ResultSet rs = mock( ResultSet.class );
  private DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
  private DatabaseMetaData dbMetaDataMock = mock( DatabaseMetaData.class );
  private LoggingObjectInterface log = mock( LoggingObjectInterface.class );
  private DatabaseInterface databaseInterface = mock( DatabaseInterface.class );

  private DatabaseMeta meta = mock( DatabaseMeta.class );
  private PreparedStatement ps = mock( PreparedStatement.class );
  private DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
  private ResultSetMetaData rsMetaData = mock( ResultSetMetaData.class );
  private Connection conn;
  //end common fields

  @BeforeClass
  public static void setUpClass() throws Exception {
    HopClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    conn = mockConnection( mock( DatabaseMetaData.class ) );
    when( log.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    if ( !NamingManager.hasInitialContextFactoryBuilder() ) {
      // If JNDI is not initialized, use simpleJNDI
      System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
        "org.osjava.sj.memory.MemoryContextFactory" ); // pentaho#simple-jndi;1.0.0
      System.setProperty( "org.osjava.sj.jndi.shared", "true" );
      InitialContextFactoryBuilder simpleBuilder = new SimpleNamingContextBuilder();
      NamingManager.setInitialContextFactoryBuilder( simpleBuilder );
    }
  }

  @After
  public void tearDown() throws NamingException {
    InitialContext ctx = new InitialContext();
    ctx.unbind( fullJndiName );
  }

  @Test
  public void testConnectJNDI() throws SQLException, NamingException, HopDatabaseException {
    InitialContext ctx = new InitialContext();
    String jndiName = "testJNDIName";
    when( meta.getName() ).thenReturn( "testName" );
    when( meta.getDatabaseName() ).thenReturn( jndiName );
    when( meta.getDisplayName() ).thenReturn( "testDisplayName" );
    when( meta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    when( meta.environmentSubstitute( jndiName ) ).thenReturn( jndiName );

    DataSource ds = mock( DataSource.class );
    when( ds.getConnection() ).thenReturn( conn );
    ctx.bind( fullJndiName, ds );

    Database db = new Database( log, meta );
    db.connect();
    assertEquals( conn, db.getConnection() );
  }


  @Test
  public void testGetQueryFieldsFromDatabaseMetaData() throws Exception {
    DatabaseMeta meta = mock( DatabaseMeta.class );
    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    Connection conn = mockConnection( dbMetaData );
    ResultSet rs = mock( ResultSet.class );
    String columnName = "year";
    String columnType = "Integer";
    int columnSize = 15;

    when( dbMetaData.getColumns( anyString(), anyString(), anyString(), anyString() ) ).thenReturn( rs );
    when( rs.next() ).thenReturn( true ).thenReturn( false );
    when( rs.getString( "COLUMN_NAME" ) ).thenReturn( columnName );
    when( rs.getString( "SOURCE_DATA_TYPE" ) ).thenReturn( columnType );
    when( rs.getInt( "COLUMN_SIZE" ) ).thenReturn( columnSize );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFromDatabaseMetaData();

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getOriginalColumnTypeName(), columnType );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getLength(), columnSize );
  }



  /**
   * PDI-11363. when using getLookup calls there is no need to make attempt to retrieve row set metadata for every call.
   * That may bring performance penalty depends on jdbc driver implementation. For some drivers that penalty can be huge
   * (postgres).
   * <p/>
   * During the execution calling getLookup() method we changing usually only lookup where clause which will not impact
   * return row structure.
   *
   * @throws HopDatabaseException
   * @throws SQLException
   */
  @Test
  public void testGetLookupMetaCalls() throws HopDatabaseException, SQLException {
    when( meta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn( "a" );
    when( meta.quoteField( anyString() ) ).thenReturn( "a" );
    when( ps.executeQuery() ).thenReturn( rs );
    when( rs.getMetaData() ).thenReturn( rsMetaData );
    when( rsMetaData.getColumnCount() ).thenReturn( 0 );
    when( ps.getMetaData() ).thenReturn( rsMetaData );
    Database db = new Database( log, meta );
    Connection conn = mock( Connection.class );
    when( conn.prepareStatement( anyString() ) ).thenReturn( ps );

    db.setConnection( conn );
    String[] name = new String[] { "a" };
    db.setLookup( "a", name, name, name, name, "a" );
    for ( int i = 0; i < 10; i++ ) {
      db.getLookup();
    }
    verify( rsMetaData, times( 1 ) ).getColumnCount();
  }

  /**
   * Test that for every PreparedStatement passed into lookup signature we do reset and re-create row meta.
   *
   * @throws SQLException
   * @throws HopDatabaseException
   */
  @Test
  public void testGetLookupCallPSpassed() throws SQLException, HopDatabaseException {
    when( ps.executeQuery() ).thenReturn( rs );
    when( rs.getMetaData() ).thenReturn( rsMetaData );
    when( rsMetaData.getColumnCount() ).thenReturn( 0 );
    when( ps.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.getLookup( ps );
    verify( rsMetaData, times( 1 ) ).getColumnCount();
  }

  @Test
  public void testCreateHopDatabaseBatchExceptionNullUpdatesWhenSQLException() {
    assertNull( Database.createHopDatabaseBatchException( "", new SQLException() ).getUpdateCounts() );
  }

  @Test
  public void testCreateHopDatabaseBatchExceptionNotUpdatesWhenBatchUpdateException() {
    assertNotNull(
      Database.createHopDatabaseBatchException( "", new BatchUpdateException( new int[ 0 ] ) ).getUpdateCounts() );
  }

  @Test
  public void testCreateHopDatabaseBatchExceptionConstructsExceptionList() {
    BatchUpdateException root = new BatchUpdateException();
    SQLException next = new SQLException();
    SQLException next2 = new SQLException();
    root.setNextException( next );
    next.setNextException( next2 );
    List<Exception> exceptionList = Database.createHopDatabaseBatchException( "", root ).getExceptionsList();
    assertEquals( 2, exceptionList.size() );
    assertEquals( next, exceptionList.get( 0 ) );
    assertEquals( next2, exceptionList.get( 1 ) );
  }

  @Test( expected = HopDatabaseBatchException.class )
  public void testInsertRowWithBatchAlwaysThrowsHopBatchException() throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection conn = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( conn );
    database.insertRow( ps, true, true );
  }

  @Test( expected = HopDatabaseException.class )
  public void testInsertRowWithoutBatchDoesntThrowHopBatchException() throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeUpdate() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setConnection( conn );
    try {
      database.insertRow( ps, true, true );
    } catch ( HopDatabaseBatchException e ) {
      // noop
    }
  }

  @Test( expected = HopDatabaseBatchException.class )
  public void testEmptyAndCommitWithBatchAlwaysThrowsHopBatchException()
    throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.emptyAndCommit( ps, true, 1 );
  }

  @Test( expected = HopDatabaseException.class )
  public void testEmptyAndCommitWithoutBatchDoesntThrowHopBatchException()
    throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( log, meta );
    database.setConnection( mockConnection );
    try {
      database.emptyAndCommit( ps, true, 1 );
    } catch ( HopDatabaseBatchException e ) {
      // noop
    }
  }

  @Test( expected = HopDatabaseBatchException.class )
  public void testInsertFinishedWithBatchAlwaysThrowsHopBatchException()
    throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertFinished( ps, true );
  }

  @Test( expected = HopDatabaseException.class )
  public void testInsertFinishedWithoutBatchDoesntThrowHopBatchException()
    throws HopDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( log, meta );
    database.setConnection( mockConnection );
    try {
      database.insertFinished( ps, true );
    } catch ( HopDatabaseBatchException e ) {
      // noop
    }
  }

  @Test
  public void insertRowAndExecuteBatchCauseNoErrors() throws Exception {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.insertRow( ps, true, false );
    verify( ps ).addBatch();

    db.executeAndClearBatch( ps );
    verify( ps ).executeBatch();
    verify( ps ).clearBatch();
  }

  @Test
  public void insertRowWhenDbDoNotSupportBatchLeadsToCommit() throws Exception {
    when( meta.supportsBatchUpdates() ).thenReturn( false );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( false );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.insertRow( ps, true, false );
    verify( ps, never() ).addBatch();
    verify( ps ).executeUpdate();
  }

  @Test
  public void testGetCreateSequenceStatement() throws Exception {
    when( meta.supportsSequences() ).thenReturn( true );
    when( meta.supportsSequenceNoMaxValueOption() ).thenReturn( true );
    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.getCreateSequenceStatement( "schemaName", "seq", "10", "1", "-1", false );
    verify( databaseInterface, times( 1 ) ).getSequenceNoMaxValueOption();
  }

  @Test
  public void testPrepareSQL() throws Exception {
    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.prepareSQL( "SELECT * FROM DUMMY" );
    db.prepareSQL( "SELECT * FROM DUMMY", true );

    verify( databaseInterface, times( 2 ) ).supportsAutoGeneratedKeys();
  }

  @Test
  public void testGetCreateTableStatement() throws Exception {
    ValueMetaInterface v = mock( ValueMetaInterface.class );
    doReturn( " " ).when( databaseInterface ).getDataTablespaceDDL( any( VariableSpace.class ), eq( meta ) );
    doReturn( "CREATE TABLE " ).when( databaseInterface ).getCreateTableStatement();

    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();
    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    String tableName = "DUMMY", tk = "tKey", pk = "pKey";
    RowMetaInterface fields = mock( RowMetaInterface.class );
    doReturn( 1 ).when( fields ).size();
    doReturn( v ).when( fields ).getValueMeta( 0 );
    boolean useAutoInc = true, semiColon = true;

    doReturn( "double foo" ).when( meta ).getFieldDefinition( v, tk, pk, useAutoInc );
    doReturn( true ).when( meta ).requiresCreateTablePrimaryKeyAppend();
    String statement = db.getCreateTableStatement( tableName, fields, tk, useAutoInc, pk, semiColon );
    String expectedStatRegexp = concatWordsForRegexp(
      "CREATE TABLE DUMMY", "\\(",
      "double foo", ",",
      "PRIMARY KEY \\(tKey\\)", ",",
      "PRIMARY KEY \\(pKey\\)",
      "\\)", ";" );
    assertTrue( statement.matches( expectedStatRegexp ) );
    doReturn( "CREATE COLUMN TABLE " ).when( databaseInterface ).getCreateTableStatement();
    statement = db.getCreateTableStatement( tableName, fields, tk, useAutoInc, pk, semiColon );

    expectedStatRegexp = concatWordsForRegexp(
      "CREATE COLUMN TABLE DUMMY", "\\(",
      "double foo", ",",
      "PRIMARY KEY \\(tKey\\)", ",",
      "PRIMARY KEY \\(pKey\\)",
      "\\)", ";" );
    assertTrue( statement.matches( expectedStatRegexp ) );
  }

  @Test
  public void testCheckTableExistsByDbMeta_Success() throws Exception {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertTrue( "The table " + EXISTING_TABLE_NAME + " is not in db meta data but should be here",
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableNotExistsByDbMeta() throws Exception {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertFalse( "The table " + NOT_EXISTING_TABLE_NAME + " is in db meta data but should not be here",
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, NOT_EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsHopDatabaseException() {
    HopDatabaseException kettleDatabaseException =
      new HopDatabaseException(
        "Unable to check if table [" + EXISTING_TABLE_NAME + "] exists on connection [" + TEST_NAME_OF_DB_CONNECTION
          + "].", SQL_EXCEPTION );
    try {
      when( dbMetaMock.getName() ).thenReturn( TEST_NAME_OF_DB_CONNECTION );
      when( rs.next() ).thenReturn( true, false );
      when( rs.getString( "TABLE_NAME" ) ).thenThrow( SQL_EXCEPTION );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown HopDatabaseException but was not." );
    } catch ( HopDatabaseException e ) {
      assertTrue( e instanceof HopDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown HopDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsHopDatabaseException_WhenDbMetaNull() {
    HopDatabaseException kettleDatabaseException =
      new HopDatabaseException( "Unable to get database meta-data from the database." );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( null ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown HopDatabaseException but was not." );
    } catch ( HopDatabaseException e ) {
      assertTrue( e instanceof HopDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown HopDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsHopDatabaseException_WhenUnableToGetTableNames() {
    HopDatabaseException kettleDatabaseException =
      new HopDatabaseException( "Unable to get table-names from the database meta-data.", SQL_EXCEPTION );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) )
        .thenThrow( SQL_EXCEPTION );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown HopDatabaseException but was not." );
    } catch ( HopDatabaseException e ) {
      assertTrue( e instanceof HopDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown HopDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsHopDatabaseException_WhenResultSetNull() {
    HopDatabaseException kettleDatabaseException =
      new HopDatabaseException( "Unable to get table-names from the database meta-data." );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) )
        .thenReturn( null );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown HopDatabaseException but was not." );
    } catch ( HopDatabaseException e ) {
      assertTrue( e instanceof HopDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown HopDatabaseException but was :" + ex.getMessage() );
    }
  }


  private Connection mockConnection( DatabaseMetaData dbMetaData ) throws SQLException {
    Connection conn = mock( Connection.class );
    when( conn.getMetaData() ).thenReturn( dbMetaData );
    return conn;
  }

  @Test
  public void usesCustomDsProviderIfSet_Pooling() throws Exception {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setUsingConnectionPool( true );
    testUsesCustomDsProviderIfSet( meta );
  }

  @Test
  public void usesCustomDsProviderIfSet_Jndi() throws Exception {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    testUsesCustomDsProviderIfSet( meta );
  }

  private DataSourceProviderInterface testUsesCustomDsProviderIfSet( DatabaseMeta meta ) throws Exception {
    Connection connection = mock( Connection.class );
    DataSource ds = mock( DataSource.class );
    when( ds.getConnection() ).thenReturn( connection );
    when( ds.getConnection( anyString(), anyString() ) ).thenReturn( connection );
    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    when( provider.getNamedDataSource( anyString(), any( DataSourceProviderInterface.DatasourceType.class ) ) )
      .thenReturn( ds );

    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( null );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    assertEquals( connection, db.getConnection() );
    return provider;
  }

  @Test
  public void jndiAccessTypePrevailsPooled() throws Exception {
    // this test is a guard of Database.normalConnect() contract:
    // it firstly tries to use JNDI name
    DatabaseMeta meta = new DatabaseMeta();
    meta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    meta.setUsingConnectionPool( true );

    DataSourceProviderInterface provider = testUsesCustomDsProviderIfSet( meta );
    verify( provider ).getNamedDataSource( anyString(), eq( DatasourceType.JNDI ) );
    verify( provider, never() ).getNamedDataSource( anyString(), eq( DatasourceType.POOLED ) );
  }

  @Test
  public void testNormalConnect_WhenTheProviderDoesNotReturnDataSourceWithPool() throws Exception {
    Driver driver = mock( Driver.class );
    when( driver.acceptsURL( anyString() ) ).thenReturn( true );
    when( driver.connect( anyString(), any( Properties.class ) ) ).thenReturn( conn );
    DriverManager.registerDriver( driver );

    when( meta.isUsingConnectionPool() ).thenReturn( true );
    when( meta.getDriverClass() ).thenReturn( driver.getClass().getName() );
    when( meta.getURL( anyString() ) ).thenReturn( "mockUrl" );
    when( meta.getInitialPoolSize() ).thenReturn( 1 );
    when( meta.getMaximumPoolSize() ).thenReturn( 1 );

    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( "ConnectThatDoesNotExistInProvider" );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    //we will check only it not null since it will be wrapped by pool and its not eqal with conn from driver
    assertNotNull( db.getConnection() );

    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testDisconnectPstmCloseFail()
    throws SQLException, HopDatabaseException, NoSuchFieldException, IllegalAccessException {
    Database db = new Database( log, meta );
    Connection connection = mockConnection( dbMetaData );
    db.setConnection( connection );
    db.setCommit( 1 );
    Class<Database> databaseClass = Database.class;
    Field fieldPstmt = databaseClass.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );
    doThrow( new SQLException( "Test SQL exception" ) ).when( ps ).close();

    db.disconnect();
    verify( connection, times( 1 ) ).close();
  }


  @Test
  public void testDisconnectCommitFail() throws SQLException, NoSuchFieldException, IllegalAccessException {
    when( meta.supportsEmptyTransactions() ).thenReturn( true );
    when( dbMetaData.supportsTransactions() ).thenReturn( true );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    db.setCommit( 1 );

    Field fieldPstmt = Database.class.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );

    doThrow( new SQLException( "Test SQL exception" ) ).when( conn ).commit();
    db.disconnect();
    verify( conn, times( 1 ) ).close();
  }


  @Test
  public void testDisconnectConnectionGroup() throws SQLException {
    Database db = new Database( log, meta );
    db.setConnection( conn );
    db.setConnectionGroup( "1" );
    db.disconnect();
    verify( conn, never() ).close();
  }

  @Test
  public void testGetTablenames() throws SQLException, HopDatabaseException {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaDataMock.getTables( any(), anyString(), anyString(), any() ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    String[] tableNames = db.getTablenames();
    assertEquals( tableNames.length, 1 );
  }

  @Test
  public void testCheckTableExistsNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkTableExists( any(), any() );
    verify( db, times( 1 ) ).checkTableExists( any() );
    verify( db, times( 0 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckTableExistsFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkTableExists( any(), any() );
    verify( db, times( 1 ) ).checkTableExists( any() );
    verify( db, times( 0 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckTableExistsTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.checkTableExists( any(), any() );
    } catch ( HopDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Unable to get table-names from the database meta-data" ) );
    }

    verify( db, times( 0 ) ).checkTableExists( any() );
    verify( db, times( 1 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckColumnExistsNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkColumnExists( any(), any(), any() );
    verify( db, times( 1 ) ).checkColumnExists( any(), any() );
    verify( db, times( 0 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testCheckColumnExistsFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkColumnExists( any(), any(), any() );
    verify( db, times( 1 ) ).checkColumnExists( any(), any() );
    verify( db, times( 0 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testCheckColumnExistsTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.checkColumnExists( any(), any(), any() );
    } catch ( HopDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Metadata check failed. Fallback to statement check." ) );
    }

    verify( db, times( 0 ) ).checkColumnExists( any(), any() );
    verify( db, times( 1 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    try {
      db.getTableFieldsMeta( any(), any() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    //verify( db, times( 1 ) ).getQueryFields( any(), any() );
    verify( db, times( 0 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.getTableFieldsMeta( any(), any() );
    //verify( db, times( 1 ) ).getQueryFields( any(), any() );
    verify( db, times( 0 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.HOP_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.getTableFieldsMeta( any(), any() );
    } catch ( HopDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Failed to fetch fields from jdbc meta" ) );
    }

    //verify( db, times( 0 ) ).getQueryFields( any(), any() );
    verify( db, times( 1 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  private String concatWordsForRegexp( String... words ) {
    String emptySpace = "\\s*";
    StringBuilder sb = new StringBuilder( emptySpace );
    for ( String word : words ) {
      sb.append( word ).append( emptySpace );
    }
    return sb.toString();
  }

  @Test
  public void testGetQueryFieldsFromPreparedStatement() throws Exception {
    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );

    when( meta.stripCR( anyString() ) ).thenReturn( sql );
    when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    when( conn.prepareStatement( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY ) ).thenReturn( ps );
    when( ps.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFromPreparedStatement( sql );

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertTrue( rowMetaInterface.getValueMeta( 0 ) instanceof ValueMetaNumber);
  }

  @Test
  public void testGetQueryFieldsFallback() throws Exception {
    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );
    when( ps.executeQuery() ).thenReturn( rs );

    when( meta.stripCR( anyString() ) ).thenReturn( sql );
    when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    when( conn.prepareStatement( sql ) ).thenReturn( ps );
    when( rs.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFallback( sql, false, null, null );

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertTrue( rowMetaInterface.getValueMeta( 0 ) instanceof ValueMetaNumber );
  }

  @Test
  public void mySqlVarBinaryIsConvertedToStringType() throws Exception {
    ResultSetMetaData rsMeta = mock( ResultSetMetaData.class );
    when( rsMeta.getColumnCount() ).thenReturn( 1 );
    when( rsMeta.getColumnLabel( 1 ) ).thenReturn( "column" );
    when( rsMeta.getColumnName( 1 ) ).thenReturn( "column" );
    when( rsMeta.getColumnType( 1 ) ).thenReturn( java.sql.Types.VARBINARY );
    when( rs.getMetaData() ).thenReturn( rsMeta );
    when( ps.executeQuery() ).thenReturn( rs );

    DatabaseMeta meta = new DatabaseMeta();
    meta.setDatabaseInterface( new MySQLDatabaseMeta() );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.getLookup( ps, false );

    RowMetaInterface rowMeta = db.getReturnRowMeta();
    assertEquals( 1, db.getReturnRowMeta().size() );

    ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
    assertEquals( ValueMetaInterface.TYPE_BINARY, valueMeta.getType() );
  }


}
