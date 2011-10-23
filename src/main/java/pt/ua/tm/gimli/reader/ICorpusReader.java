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
package pt.ua.tm.gimli.reader;

import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.external.gdep.GDepCorpus;

/**
 * Interface of corpus readers.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public interface ICorpusReader {
    /**
     * Read corpus from input file and convert it into Gimli's format.
     * @param format Label encoding format to be used in this corpus.
     * @return The corpus with all the annotations.
     * @throws GimliException Problem reading the input file.
     */
    Corpus read(LabelFormat format) throws GimliException;
    
    /**
     * Get the GDep parsing output of the corpus.
     * @return The result of parsing the corpus using GDep.
     * @throws GimliException Problem parsing the corpus.
     */
    GDepCorpus getGDepCorpus() throws GimliException;
}
