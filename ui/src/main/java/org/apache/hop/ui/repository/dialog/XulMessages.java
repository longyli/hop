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

package org.apache.hop.ui.repository.dialog;

import org.apache.hop.i18n.BaseMessages;

public class XulMessages implements org.pentaho.xul.Messages {
  private static Class<?> PKG = XulMessages.class; // for i18n purposes, needed by Translator2!!

  public String getString( String key, String... parameters ) {
    return BaseMessages.getString( PKG, key, parameters );
  }
}
