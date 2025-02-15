/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.apache.hop.metastore.api.exceptions;

/**
 * This exception is thrown in case a namespace is created in a metadata store when it already exists.
 * 
 * @author matt
 * 
 */

public class MetaStoreNamespaceExistsException extends MetaStoreException {

  private static final long serialVersionUID = 2614122560674499038L;

  public MetaStoreNamespaceExistsException() {
    super();
  }

  public MetaStoreNamespaceExistsException( String message ) {
    super( message );
  }

  public MetaStoreNamespaceExistsException( Throwable cause ) {
    super( cause );
  }

  public MetaStoreNamespaceExistsException( String message, Throwable cause ) {
    super( message, cause );
  }
}
