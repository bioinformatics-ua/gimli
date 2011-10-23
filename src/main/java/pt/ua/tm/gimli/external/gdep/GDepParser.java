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
package pt.ua.tm.gimli.external.gdep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.external.wrapper.Parser;

/**
 * GDep Parser wrapper.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepParser extends Parser {

    /**
     * Constructor.
     * @param doTokenisation <code>True</code> to use GDep tokenisation, and
     * <code>False</code> otherwise.
     */
    public GDepParser(final boolean doTokenisation) {
        super();
        if (doTokenisation) {
            parserCommand = Constants.GDEPCOMMAND;
        } else {
            parserCommand = Constants.GDEPCOMMANDNT;
        }
    }

    /**
     * Parse a sentence using GDep.
     * @param sentence The sentence to be parsed.
     * @return Output of GDep parser.
     * @throws GimliException Problem parsing the sentence.
     */
    @Override
    public List<Object> parse(final String sentence) throws GimliException {
        if (!hasInstance) {
            return null;
        }

        List<Object> results = new ArrayList<Object>();
        try {

            bw.write(sentence.trim() + "\n");
            bw.flush();

            while (!br.ready()) {
                // wait for results
                Thread.yield();
            }
            String line;

            while (!( line = br.readLine() ).equalsIgnoreCase("")) {
                results.add(line);
            }
        }
        catch (IOException ex) {
            throw new GimliException("An error occured while parsing the sentence.", ex);
        }

        return results;
    }
}
