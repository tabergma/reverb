package edu.washington.cs.knowitall.extractor.mapper;

import java.util.HashSet;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedRelationExtraction;
import edu.washington.cs.knowitall.normalization.NormalizedField;
import edu.washington.cs.knowitall.normalization.VerbalRelationNormalizer;

/**
 * A class used to filter out any relations whose normalized form does not appear in the given
 * dictionary. Relation strings are normalized using the VerbalRelationNormalizer class.
 *
 * @author afader
 */
public class NormalizedRelationDictionaryFilter extends
                                                FilterMapper<ChunkedRelationExtraction> {

    private HashSet<String> relations;
    private VerbalRelationNormalizer normalizer;

    /**
     * Constructs a new filter using the String relations in the given set. These relations should
     * be normalized using the VerbalRelationNormalizer class, with a space between each token in
     * the string.
     */
    public NormalizedRelationDictionaryFilter(HashSet<String> relations) {
        this.relations = relations;
        normalizer = new VerbalRelationNormalizer(true, true);
    }

    /**
     * Returns true if the tokens in the given extraction appear in the set of relations passed to
     * the constructor.
     */
    public boolean doFilter(ChunkedRelationExtraction extr) {
        NormalizedField normField = normalizer.normalizeField(extr);
        return relations.contains(normField.toString());
    }

}
