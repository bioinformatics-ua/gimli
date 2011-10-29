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
