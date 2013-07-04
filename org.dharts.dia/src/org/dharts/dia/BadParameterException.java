/* Created on       Jun 15, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.dharts.dia;


/** 
 * Indicates that an invalid configuration parameter was supplied for an
 * algorithm. This may be due to a number of different causes, the  most
 * common of which are that the specified parameter does not exist, an invalid
 * value was supplied, or the value of this parameter conflicts with the values
 * supplied for other parameters. The exception error message should explain
 * the cause of the exception. 
 *  
 * @author Neal Audenaert
 */
public class BadParameterException extends DIAException {

    public BadParameterException(String msg) { 
        super(msg);
    }

}
