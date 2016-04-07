package edu.washington.cs.knowitall.extractor;

import com.google.common.collect.Iterables;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import edu.washington.cs.knowitall.extractor.mapper.ReVerbRelationDictionaryFilter;
import edu.washington.cs.knowitall.extractor.mapper.ReVerbRelationMappers;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedRelationExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.util.DefaultObjects;


public abstract class ReVerbRelationExtractor extends RelationFirstNpChunkExtractor {

    /**
     * Definition of the "verb" of the relation pattern.
     */
    public static final String VERB =
        // Optional adverb
        "[ADV_pos PAV_pos]? " +
        // Modal or other verbs
        "[VVFIN_pos VVIMP_pos VVINF_pos VVIZU_pos VVPP_pos VAFIN_pos VAIMP_pos VAINF_pos VAPP_pos VMFIN_pos VMINF_pos VMPP_pos PTKVZ_pos] "
        +
        // Optional particle/adverb
        "[PTKNEG_pos PTKVZ_pos]?";

    /**
     * Definition of the "non-verb/prep" part of the relation pattern.
     */
    public static final String WORD =
        "[NE_pos NN_pos ART_pos ADJA_pos ADV_pos CARD_pos]";

    /**
     * Definition of the "preposition" part of the relation pattern.
     */
    public static final String PREP =
        "[APPR_pos APPRART_pos PROAV_pos ART_pos ADJD_pos PPOSAT_pos]";

    /**
     * The pattern (V(W*P)?)+
     */
    public static final String LONG_RELATION_PATTERN =
        String.format("(%s (%s* (%s)+)?)+", VERB, WORD, PREP);

    /**
     * The pattern (VP?)+
     */
    public static final String SHORT_RELATION_PATTERN =
        String.format("(%s (%s)?)+", VERB, PREP);

    /**
     * Constructs a new extractor using the default relation pattern, relation mappers, and argument
     * mappers.
     *
     * @throws ExtractorException if unable to initialize the extractor
     */
    public ReVerbRelationExtractor() throws ExtractorException {
        initializeRelationExtractor();
        initializeArgumentExtractors();
    }


    /**
     * Constructs a new extractor using the default relation pattern, relation mappers, and argument
     * mappers.
     *
     * @param minFreq              - The minimum distinct arguments to be observed in a large
     *                             collection for the relation to be deemed valid.
     * @param useLexSynConstraints - Use syntactic and lexical constraints that are part of Reverb?
     * @param mergeOverlapRels     - Merge overlapping relations?
     * @param allowUnary           - Allow relations with one argument to be output.
     * @throws ExtractorException if unable to initialize the extractor
     */
    public ReVerbRelationExtractor(int minFreq, boolean useLexSynConstraints,
                                   boolean mergeOverlapRels, boolean allowUnary)
        throws ExtractorException {
        initializeRelationExtractor(minFreq, useLexSynConstraints, mergeOverlapRels, allowUnary);
        initializeArgumentExtractors();
    }

    protected abstract void initializeArgumentExtractors();

    /**
     * Wrapper for default initialization of the reverb relation extractor. Use lexical and
     * syntactic constraints, merge overlapping relations,require a minimum of 20 distinct arguments
     * for support, and do not allow unary relations.
     */
    protected void initializeRelationExtractor() throws ExtractorException {
        initializeRelationExtractor(ReVerbRelationDictionaryFilter.defaultMinFreq, true, true,
                                    false);
    }

    /**
     * Initialize relation extractor.
     *
     * @param minFreq              - The minimum distinct arguments to be observed in a large
     *                             collection for the relation to be deemed valid.
     * @param useLexSynConstraints - Use syntactic and lexical constraints that are part of Reverb?
     * @param mergeOverlapRels     - Merge overlapping relations?
     * @param allowUnary           - Allow relations with one argument to be output.
     * @throws ExtractorException if unable to initialize the extractor
     */
    protected void initializeRelationExtractor(int minFreq, boolean useLexSynConstraints,
                                               boolean mergeOverlapRels, boolean allowUnary)
        throws ExtractorException {
        ExtractorUnion<ChunkedSentence, ChunkedRelationExtraction> relExtractor =
            new ExtractorUnion<ChunkedSentence, ChunkedRelationExtraction>();

        try {
            relExtractor.addExtractor(new RegexExtractor(SHORT_RELATION_PATTERN));
        } catch (SequenceException e) {
            throw new ExtractorException(
                "Unable to initialize short pattern extractor", e);
        }

        try {
            relExtractor.addExtractor(new RegexExtractor(LONG_RELATION_PATTERN));
        } catch (SequenceException e) {
            throw new ExtractorException(
                "Unable to initialize long pattern extractor", e);
        }
        try {
            relExtractor.addMapper(
                new ReVerbRelationMappers(minFreq, useLexSynConstraints, mergeOverlapRels));
        } catch (IOException e) {
            throw new ExtractorException(
                "Unable to initialize relation mappers", e);
        }

        // Hack to allow unary relations.
        super.setAllowUnary(allowUnary);

        setRelationExtractor(relExtractor);
    }

    /**
     * Extracts from the given text using the default sentence reader returned by {@link
     * DefaultObjects#getDefaultSentenceReader(java.io.Reader)}.
     *
     * @return an iterable object over the extractions
     * @throws ExtractorException if unable to extract
     */
    public Iterable<ChunkedBinaryExtraction> extractFromString(String text)
        throws ExtractorException {
        try {
            StringReader in = new StringReader(text);
            return extractUsingReader(
                DefaultObjects.getDefaultSentenceReader(in));
        } catch (IOException e) {
            throw new ExtractorException(e);
        }

    }

    /**
     * Extracts from the given reader
     *
     * @return extractions
     * @throws ExtractorException if unable to extract
     */
    private Iterable<ChunkedBinaryExtraction> extractUsingReader(
        ChunkedSentenceReader reader) throws ExtractorException {

        ArrayList<ChunkedBinaryExtraction> results =
            new ArrayList<ChunkedBinaryExtraction>();

        Iterable<ChunkedSentence> sents =
            reader.getSentences();
        for (ChunkedSentence sent : sents) {
            Iterables.addAll(results, extract(sent));
        }
        return results;
    }
}

