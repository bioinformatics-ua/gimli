/*
 * Gimli - High-performance and multi-corpus recognition of biomedical
 * entity names
 *
 * Copyright (C) 2011 David Campos, Universidade de Aveiro, Instituto de
 * Engenharia Electrónica e Telemática de Aveiro
 *
 * Gimli is licensed under the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * Gimli is a free software, you are free to copy, distribute, change and
 * transmit it. However, you may not use Gimli for commercial purposes.
 *
 * Gimli is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
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
