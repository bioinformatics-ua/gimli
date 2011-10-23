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

/**
 * Process of the external executable.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class ProcessConnector {

    private InputStream is;
    private OutputStream os;
    private OutputStream es;
    private Process process;
    private Thread tis;
    private Thread tos;
    private Thread tes;

    /**
     * Constructor.
     * @param is Input data.
     * @param os Output info data.
     * @param es Output error data.
     */
    public ProcessConnector(InputStream is, OutputStream os, OutputStream es) {
        this.is = is;
        this.os = os;
        this.es = es;
    }

    /**
     * Create the process to execute the external program.
     * @param command The command line to be executed.
     * @throws IOException Problem executing the command line.
     */
    public void create(String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();
    }

    /**
     * Kill.
     */
    public void destroy() {
        process.destroy();
        tis.stop();
        tos.stop();
        tes.stop();
    }
}
