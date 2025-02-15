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

package org.apache.hop.ui.core.database.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.hopui.HopUi;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out which fields are output by a
 * certain SQL query on a database.
 *
 * @author Matt
 * @since 12-may-2005
 */
public class GetQueryFieldsProgressDialog {
  private static Class<?> PKG = GetQueryFieldsProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private DatabaseMeta dbMeta;
  private String sql;
  private RowMetaInterface result;

  private Database db;

  /**
   * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
   * database.
   */
  public GetQueryFieldsProgressDialog( Shell shell, DatabaseMeta dbInfo, String sql ) {
    this.shell = shell;
    this.dbMeta = dbInfo;
    this.sql = sql;
  }

  public RowMetaInterface open() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        db = new Database( HopUi.loggingObject, dbMeta );
        try {
          db.connect();
          result = db.getQueryFields( sql, false );
          if ( monitor.isCanceled() ) {
            throw new InvocationTargetException( new Exception( "This operation was cancelled!" ) );
          }
        } catch ( Exception e ) {
          throw new InvocationTargetException( e, "Problem encountered determining query fields: " + e.toString() );
        } finally {
          db.disconnect();
        }
      }
    };

    try {
      final ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );

      // Run something in the background to cancel active database queries, forecably if needed!
      Runnable run = new Runnable() {
        public void run() {
          IProgressMonitor monitor = pmd.getProgressMonitor();
          while ( pmd.getShell() == null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ) ) {
            try {
              Thread.sleep( 250 );
            } catch ( InterruptedException e ) {
              // Ignore
            }
          }

          if ( monitor.isCanceled() ) { // Disconnect and see what happens!

            try {
              db.cancelQuery();
            } catch ( Exception e ) {
              // Ignore
            }
          }
        }
      };
      // Dump the cancel looker in the background!
      new Thread( run ).start();

      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      showErrorDialog( e );
      return null;
    } catch ( InterruptedException e ) {
      showErrorDialog( e );
      return null;
    }

    return result;
  }

  /**
   * Showing an error dialog
   *
   * @param e
   */
  private void showErrorDialog( Exception e ) {
    new ErrorDialog(
      shell, BaseMessages.getString( PKG, "GetQueryFieldsProgressDialog.Error.Title" ), BaseMessages.getString(
        PKG, "GetQueryFieldsProgressDialog.Error.Message" ), e );
  }
}
