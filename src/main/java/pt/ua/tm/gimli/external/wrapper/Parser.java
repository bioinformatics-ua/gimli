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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * External parser wrapper.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public abstract class Parser {

    protected String[] parserCommand;
    protected File dir;
    protected PipedInputStream pis;
    protected PipedOutputStream sink;
    protected PipedOutputStream pos;
    protected PipedInputStream source;
    protected BufferedReader br;
    protected BufferedWriter bw;
    protected ProcessConnector pc;
    protected boolean hasInstance;

    /**
     * Launch the parser.
     * @throws IOException Problem launching the parser.
     */
    public synchronized void launch() throws IOException {
        if (hasInstance) {
            return;
        }
        hasInstance = true;
        pis = new PipedInputStream();
        sink = new PipedOutputStream(pis);
        pos = new PipedOutputStream();
        source = new PipedInputStream(pos);
        br = new BufferedReader(new InputStreamReader(source));
        bw = new BufferedWriter(new OutputStreamWriter(sink));
        pc = new ProcessConnector(pis, pos, System.err);
        
        if (dir == null){
            pc.create(parserCommand);
        } else {
            pc.create(dir, parserCommand);
        }
        
    }

    /**
     * Terminate the execution of the parser.
     */
    public synchronized void terminate() {
        if (!hasInstance) {
            return;
        }
        pc.destroy();
    }

    /**
     * Process the parsing result.
     * @param sentence The sentence to be parsed.
     * @return Output data.
     * @throws GimliException Problem parsing the sentence.
     */
    public abstract List<Object> parse(String sentence) throws GimliException;
}
