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
package pt.ua.tm.gimli.external.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map the input and output data to run external executables.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class StreamGobbler implements Runnable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
    /**
     * Input data.
     */
    private InputStream is;
    /**
     * Output data.
     */
    private OutputStream os;

    /**
     * Constructor.
     * @param is Input data
     * @param os Output data
     */
    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    /**
     * Map the input and output data.
     */
    public void run() {
        try {
            byte[] buffer = new byte[1 << 12];
            int c;
            while (( c = is.read(buffer) ) != -1) {
                os.write(buffer, 0, c);
                os.flush();
            }
        }
        catch (IOException ex) {
            logger.error("There was a problem writing the output.", ex);
            return;
        }
    }
}
