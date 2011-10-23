/*
 *  Gimli - High-performance and multi-corpus recognition of biomedical
 *  entity names
 *
 *  Copyright (C) 2011 David Campos, Universidade de Aveiro, Instituto de
 *  Engenharia Electrónica e Telemática de Aveiro
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version. Thus, this program could not be
 *  used for commercial purposes.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package pt.ua.tm.gimli.exception;

/**
 * Exception handling.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GimliException extends Exception {

    /**
     * Constructor with message.
     * @param m Associated message.
     */
    public GimliException(final String m) {
        super(m);
    }

    /**
     * Constructor with exception.
     * @param e Associated exception.
     */
    public GimliException(final Exception e) {
        super(e);
    }

    /**
     * Constructor with message and throwable exception.
     * @param m Associated message.
     * @param t Associated throwable exception.
     */
    public GimliException(final String m, final Throwable t) {
        super(m, t);
    }
}
