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

package org.apache.hop.core.exception;

/**
 * This exception is thrown in case there is an error in the Hop plugin loader
 *
 * @author matt
 *
 */
public class HopPluginException extends HopException {

  private static final long serialVersionUID = -7251001771637436705L;

  public HopPluginException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public HopPluginException( String message, Throwable cause ) {
    super( message, cause );
  }

  /**
   * @param message
   */
  public HopPluginException( String message ) {
    super( message );
  }

  /**
   * @param cause
   */
  public HopPluginException( Throwable cause ) {
    super( cause );
  }

}
