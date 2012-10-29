/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.hp.parallel.messages;
/**
 * Exception message. It is used for exception handling. It contains an error code and a detailed error text.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |    
 * +--+--+--+--+--+--+--+--+
 * |       Error Code      |
 * +--+--+--+--+--+--+--+--+
 * |      Text Length      |
 * +--+--+--+--+--+--+--+--+
 * |                       |
 * .       Error Text      .
 * .  (Maximum 255 Bytes)  .
 * .                       .
 * +--+--+--+--+--+--+--+--+
 *
 *</pre> 
 * @author Daniel Maier
 *
 */
public final class ExceptionMessage implements Message {
	private final Error error;
	
	/**
	 * Creates a new ExceptionMessage with the given error.
	 * @param error error to be encoded by the ExceptionMessage.
	 */
	public ExceptionMessage(Error error) {
		this.error = error;
	}

	/**
	 * Returns the error code of the exception.
	 * @return error code of the exception.
	 */
	public int getErrorCode() {
		return error.getCode();
	}

	/**
	 * Returns the error text of the exception.
	 * @return error text of the exception.
	 */
	public String getErrorText() {
		return error.getText();
	}
	
	/**
	 * Returns the enum error representation of 
	 * the message.
	 * @return the enum error representation
	 */
	public Error getError() {
		return error;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ExceptionMessage)) {
			return false;
		}
		ExceptionMessage other = (ExceptionMessage) obj;
		if (error == null) {
			if (other.error != null) {
				return false;
			}
		} else if (!error.equals(other.error)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ExceptionMessage [error=" + error + "]";
	}



	public static enum Error {
		  TargetNotRegistered(0, "The desired target is not currently registered."),
		  UnknownMessage(1, "The submitted message is unknown");

		  private final int code;
		  private final String text;

		  private Error(int code, String text) {
		    this.code = code;
		    this.text = text;
		  }

		  public String getText() {
		     return text;
		  }

		  public int getCode() {
		     return code;
		  }
		  
		  @Override
		  public String toString() {
			  return "Error [code=" + code + ", text="
				+ text + "]";
		  }
	}
}
