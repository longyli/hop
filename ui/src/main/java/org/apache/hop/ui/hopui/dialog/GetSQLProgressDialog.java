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

package org.apache.hop.ui.hopui.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.ProgressMonitorAdapter;
import org.apache.hop.core.SQLStatement;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a transformation...
 *
 * @author Matt
 * @since 15-mrt-2005
 */
public class GetSQLProgressDialog {
  private static Class<?> PKG = GetSQLProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private TransMeta transMeta;
  private List<SQLStatement> stats;

  /**
   * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
   */
  public GetSQLProgressDialog( Shell shell, TransMeta transMeta ) {
    this.shell = shell;
    this.transMeta = transMeta;
  }

  public List<SQLStatement> open() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        // This is running in a new process: copy some HopVariables info
        // LocalVariables.getInstance().createHopVariables(Thread.currentThread(), parentThread, true);
        // --> don't set variables if not running in different thread --> pmd.run(true,true, op);

        try {
          stats = transMeta.getSQLStatements( new ProgressMonitorAdapter( monitor ) );
        } catch ( HopException e ) {
          throw new InvocationTargetException( e, BaseMessages.getString(
            PKG, "GetSQLProgressDialog.RuntimeError.UnableToGenerateSQL.Exception", e.getMessage() ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( false, false, op );
    } catch ( InvocationTargetException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title" ),
        BaseMessages.getString( PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message" ), e );
      stats = null;
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title" ),
        BaseMessages.getString( PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message" ), e );
      stats = null;
    }

    return stats;
  }
}
