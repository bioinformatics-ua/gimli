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
package pt.ua.tm.gimli.corpus;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Used to sort a set of {@link Annotation} objects.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class AnnotationComparator implements
        Comparator<Annotation>, Serializable {

    /**
     * Compare two annotations, considering one bigger than the other or equal.
     * @param t 1st annotation to be compared.
     * @param t1 2nd annotation to be compared.
     * @return <code>1</code> if the 1st annotation appears latter in the
     * sentence, and <code>-1</code> if the 2nd annotation appears latter
     * in the sentence.
     */
    @Override
    public int compare(final Annotation t, final Annotation t1) {

        if (t.getStartIndex() > t1.getStartIndex()) {
            return 1;
        }
        if (t.getStartIndex() < t1.getStartIndex()) {
            return -1;
        }

        if (t.getEndIndex() > t1.getEndIndex()) {
            return 1;
        }
        if (t.getEndIndex() < t1.getEndIndex()) {
            return -1;
        }
        return 0;
    }
}
