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

package org.apache.hop.core.database;

import org.apache.hop.core.Const;
import org.apache.hop.core.plugins.DatabaseMetaPlugin;
import org.apache.hop.core.row.ValueMetaInterface;

/**
 * Contains Sybase specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

@DatabaseMetaPlugin(
        type = "SYBASE",
        typeDescription = "Sybase"
)
public class SybaseDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 5001;
    }
    return -1;
  }

  /**
   * @see org.apache.hop.core.database.DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean use_autoinc ) {
    if ( supportsAutoInc() && use_autoinc ) {
      return 1;
    }
    return super.getNotFoundTK( use_autoinc );
  }

  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "net.sourceforge.jtds.jdbc.Driver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else {
      // jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
      return "jdbc:jtds:sybase://" + hostname + ":" + port + "/" + databaseName;
    }
  }

  /**
   * @see org.apache.hop.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
   */
  @Override
  public String getSchemaTableCombination( String schema_name, String table_part ) {
    return table_part;
  }

  /**
   * @return true if this database needs a transaction to perform a query (auto-commit turned off).
   */
  @Override
  public boolean isRequiringTransactionsOnQueries() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( add_fieldname ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval += "DATETIME NULL";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        if ( supportsBooleanDataType() ) {
          retval += "BOOLEAN";
        } else {
          retval += "CHAR(1)";
        }
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key: auto increment field!
          fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          if ( use_autoinc ) {
            retval += "INTEGER IDENTITY NOT NULL";
          } else {
            retval += "INTEGER NOT NULL PRIMARY KEY";
          }
        } else {
          if ( precision != 0 || ( precision == 0 && length > 9 ) ) {
            if ( precision > 0 && length > 0 ) {
              retval += "DECIMAL(" + length + ", " + precision + ") NULL";
            } else {
              retval += "DOUBLE PRECISION NULL";
            }
          } else {
            // Precision == 0 && length<=9
            if ( length < 3 ) {
              retval += "TINYINT NULL";
            } else if ( length < 5 ) {
              retval += "SMALLINT NULL";
            } else {
              retval += "INTEGER NULL";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= 2048 ) {
          retval += "TEXT NULL";
        } else {
          retval += "VARCHAR";
          if ( length > 0 ) {
            retval += "(" + length + ")";
          }
          retval += " NULL";
        }
        break;
      default:
        retval += " UNKNOWN";
        break;
    }

    if ( add_cr ) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public String getExtraOptionsHelpText() {
    return "http://jtds.sourceforge.net/faq.html#urlFormat";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "jtds-1.2.jar" };
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *          the schema-table name to insert into
   * @param keyField
   *          The key field
   * @param versionField
   *          the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return "insert into " + schemaTable + "(" + versionField + ") values (1)";
  }

  /**
   * @param string
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  @Override
  public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "''" );
    string = string.replaceAll( "\\n", "\\0xd" );
    string = string.replaceAll( "\\r", "\\0xa" );
    return "'" + string + "'";
  }

  @Override
  public boolean isSybaseVariant(){return true;}
}
