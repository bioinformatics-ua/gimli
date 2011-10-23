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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Represents the corpus provided by GDep parser after parsing the corpus.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepCorpus {
    /**
     * The corpus is composed by a set of sentences.
     */
    private ArrayList<GDepSentence> corpus;

    /**
     * Constructor.
     */
    public GDepCorpus() {
        this.corpus = new ArrayList<GDepSentence>();
    }

    /**
     * Add sentence.
     * @param s GDep sentence to be added.
     */
    public void addSentence(final GDepSentence s) {
        corpus.add(s);
    }

    /**
     * Get specific sentence.
     * @param i The index of the sentence.
     * @return The sentence in the <code>i</code> position of the
     * {@link ArrayList}.
     */
    public GDepSentence getSentence(final int i) {
        return corpus.get(i);
    }

    /**
     * The number of sentences of the GDep corpus.
     * @return The number of sentences.
     */
    public int size() {
        return corpus.size();
    }
    
    /**
     * Write the GDep parsing result of the corpus in a file.
     * @param file The file to store the GDep corpus.
     * @throws GimliException Problem writing the file.
     */
    public void writeToFile(final String file) throws GimliException {
        try {
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file));

            for (GDepSentence s : corpus) {
                for (int i = 0; i < s.size(); i++) {
                    out.write(s.getToken(i).toString().getBytes());
                    out.write("\n".getBytes());
                }
                out.write("\n".getBytes());
            }
            out.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem writing the output file.", ex);
        }
    }

    /**
     * Load the GDep Output from a previously written file.
     * @param file File that contains the GDep corpus.
     * @throws GimliException Problem reading the file.
     */
    public void loadFromFile(final String file) throws GimliException {
        this.corpus = new ArrayList<GDepSentence>();

        try {
            InputStreamReader isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
            BufferedReader br = new BufferedReader(isr);
            String line;
            GDepSentence s = new GDepSentence(this);
            GDepToken gt;
            String[] parts;
            

            while (( line = br.readLine() ) != null) {
                if (line.equals("") || line.equals("\n")) {
                    if (s.size() > 0) {
                        this.addSentence(s);
                    }
                    s = new GDepSentence(this);
                } else {
                    parts = line.split("\t");
                    gt = new GDepToken(parts[0], parts[1], parts[2], parts[3], Integer.valueOf(parts[4]), parts[5]);
                    s.addToken(gt);
                }
            }

            br.close();
            isr.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem reading the GDep file.", ex);
        }
    }
}
