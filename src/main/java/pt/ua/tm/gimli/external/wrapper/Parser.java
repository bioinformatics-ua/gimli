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

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
        pc.create(parserCommand);
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
