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

package org.apache.hop.core.util;

import org.mozilla.javascript.Context;
import org.apache.hop.compatibility.Value;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.xml.XMLHandler;

import java.math.BigDecimal;
import java.util.Date;

/**
 * This class contains common code from {@linkplain org.apache.hop.trans.steps.script.Script} and {@linkplain
 * org.apache.hop.trans.steps .scriptvalues_mod.ScriptValuesMod} classes
 *
 * @author Andrey Khayrutdinov
 */
public class JavaScriptUtils {

  public static Object convertFromJs( Object value, int type, String fieldName ) throws HopValueException {
    String classType = value.getClass().getName();
    switch ( type ) {
      case ValueMetaInterface.TYPE_NUMBER:
        return jsToNumber( value, classType );

      case ValueMetaInterface.TYPE_INTEGER:
        return jsToInteger( value, value.getClass() );

      case ValueMetaInterface.TYPE_STRING:
        return jsToString( value, classType );

      case ValueMetaInterface.TYPE_DATE:
        return jsToDate( value, classType );

      case ValueMetaInterface.TYPE_BOOLEAN:
        return value;

      case ValueMetaInterface.TYPE_BIGNUMBER:
        return jsToBigNumber( value, classType );

      case ValueMetaInterface.TYPE_BINARY: {
        return Context.jsToJava( value, byte[].class );
      }
      case ValueMetaInterface.TYPE_NONE: {
        throw new RuntimeException( "No data output data type was specified for new field ["
          + fieldName + "]" );
      }
      default:
        return Context.jsToJava( value, Object.class );
    }
  }

  public static Number jsToNumber( Object value, String classType ) {
    if ( classType.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
      return null;
    } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
      try {
        // Is it a java Value class ?
        Value v = (Value) Context.jsToJava( value, Value.class );
        return v.getNumber();
      } catch ( Exception e ) {
        String string = Context.toString( value );
        return Double.parseDouble( Const.trim( string ) );
      }
    } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeNumber" ) ) {
      Number nb = Context.toNumber( value );
      return nb.doubleValue();
    } else {
      Number nb = (Number) value;
      return nb.doubleValue();
    }
  }

  public static Long jsToInteger( Object value, Class<?> clazz ) {
    if ( Number.class.isAssignableFrom( clazz ) ) {
      return ( (Number) value ).longValue();
    } else {
      String classType = clazz.getName();
      if ( classType.equalsIgnoreCase( "java.lang.String" ) ) {
        return ( new Long( (String) value ) );
      } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
        return null;
      } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeNumber" ) ) {
        Number nb = Context.toNumber( value );
        return nb.longValue();
      } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
        // Is it a Value?
        //
        try {
          Value v = (Value) Context.jsToJava( value, Value.class );
          return v.getInteger();
        } catch ( Exception e2 ) {
          String string = Context.toString( value );
          return Long.parseLong( Const.trim( string ) );
        }
      } else {
        return Long.parseLong( value.toString() );
      }
    }
  }

  public static String jsToString( Object value, String classType ) {
    if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" )
      || classType.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
      // Is it a java Value class ?
      try {
        Value v = (Value) Context.jsToJava( value, Value.class );
        return v.toString();
      } catch ( Exception ev ) {
        // convert to a string should work in most cases...
        //
        return Context.toString( value );
      }
    } else {
      // A String perhaps?
      return Context.toString( value );
    }
  }

  public static Date jsToDate( Object value, String classType ) throws HopValueException {
    double dbl;
    if ( classType.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
      return null;
    } else {
      if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeDate" ) ) {
        dbl = Context.toNumber( value );
      } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" )
        || classType.equalsIgnoreCase( "java.util.Date" ) ) {
        // Is it a java Date() class ?
        try {
          Date dat = (Date) Context.jsToJava( value, java.util.Date.class );
          dbl = dat.getTime();
        } catch ( Exception e ) {
          // Is it a Value?
          //
          try {
            Value v = (Value) Context.jsToJava( value, Value.class );
            return v.getDate();
          } catch ( Exception e2 ) {
            try {
              String string = Context.toString( value );
              return XMLHandler.stringToDate( string );
            } catch ( Exception e3 ) {
              throw new HopValueException( "Can't convert a string to a date" );
            }
          }
        }
      } else if ( classType.equalsIgnoreCase( "java.lang.Double" ) ) {
        dbl = (Double) value;
      } else {
        String string = (String) Context.jsToJava( value, String.class );
        dbl = Double.parseDouble( string );
      }
      long lng = Math.round( dbl );
      return new Date( lng );
    }
  }

  public static BigDecimal jsToBigNumber( Object value, String classType ) {
    if ( classType.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
      return null;
    } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeNumber" ) ) {
      Number nb = Context.toNumber( value );
      return new BigDecimal( nb.doubleValue() );
    } else if ( classType.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
      // Is it a BigDecimal class ?
      try {
        return (BigDecimal) Context.jsToJava( value, BigDecimal.class );
      } catch ( Exception e ) {
        try {
          Value v = (Value) Context.jsToJava( value, Value.class );
          if ( !v.isNull() ) {
            return v.getBigNumber();
          } else {
            return null;
          }
        } catch ( Exception e2 ) {
          String string = (String) Context.jsToJava( value, String.class );
          return new BigDecimal( string );
        }
      }
    } else if ( classType.equalsIgnoreCase( "java.lang.Byte" ) ) {
      return new BigDecimal( ( (Byte) value ).longValue() );
    } else if ( classType.equalsIgnoreCase( "java.lang.Short" ) ) {
      return new BigDecimal( ( (Short) value ).longValue() );
    } else if ( classType.equalsIgnoreCase( "java.lang.Integer" ) ) {
      return new BigDecimal( ( (Integer) value ).longValue() );
    } else if ( classType.equalsIgnoreCase( "java.lang.Long" ) ) {
      return new BigDecimal( ( (Long) value ).longValue() );
    } else if ( classType.equalsIgnoreCase( "java.lang.Double" ) ) {
      return new BigDecimal( ( (Double) value ).doubleValue() );
    } else if ( classType.equalsIgnoreCase( "java.lang.String" ) ) {
      return new BigDecimal( ( new Long( (String) value ) ).longValue() );
    } else {
      throw new RuntimeException( "JavaScript conversion to BigNumber not implemented for " + classType );
    }
  }
}
