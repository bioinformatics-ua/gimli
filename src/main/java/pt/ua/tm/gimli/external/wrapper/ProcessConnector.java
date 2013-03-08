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
package pt.ua.tm.gimli.external.wrapper;

import java.io.File;
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

    public void create(File dir, String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        pb.directory(dir);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();
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
