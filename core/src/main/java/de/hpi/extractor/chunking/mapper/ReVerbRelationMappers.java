package de.hpi.extractor.chunking.mapper;

import de.hpi.extractor.FilterMapper;
import de.hpi.extractor.MapperList;
import de.hpi.nlp.chunking.ChunkedSentence;
import de.hpi.nlp.extraction.chunking.ChunkedRelationExtraction;

import java.io.IOException;

/**
 * A list of mappers for <code>ReVerbExtractor</code>'s relations.
 *
 * @author afader
 */
public class ReVerbRelationMappers extends MapperList<ChunkedRelationExtraction> {

    /**
     * Default construction of ReVerbRelationMappers. Uses Lexical and Syntactic constraints, merges
     * overlapping relations, and requires a minimum of 20 distinct arguments for each relation on a
     * large corpus.
     * @throws IOException if the relation dictionary could not be read
     */
    public ReVerbRelationMappers() throws IOException {
        init();
    }

    public ReVerbRelationMappers(int minFreq) throws IOException {
        init(minFreq);
    }

    public ReVerbRelationMappers(int minFreq, boolean useLexSynConstraints,
                                 boolean mergeOverlapRels, boolean combineVerbs) throws IOException {
        init(minFreq, useLexSynConstraints, mergeOverlapRels, combineVerbs);
    }

    private void init() throws IOException {
        // Add lexical and syntactic constraints on the relations.
        addLexicalAndSyntacticConstraints();

        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        addMapper(new ReVerbRelationDictionaryFilter());

        // Overlapping relations should be merged together
        addMapper(new MergeOverlappingMapper());

        // Extracted relation must contain at lease one VP chunk tag
        addMapper(new VerbFilter());

    }

    private void init(int minFreq) throws IOException {
        // Add lexical and syntactic constraints on the relation.
        addLexicalAndSyntacticConstraints();

        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        addMapper(new ReVerbRelationDictionaryFilter(minFreq));

        // Overlapping relations should be merged together
        addMapper(new MergeOverlappingMapper());

        // Extracted relation must contain at lease one VP chunk tag
        addMapper(new VerbFilter());

    }

    private void init(int minFreq, boolean useLexSynConstraints,
                      boolean mergeOverlapRels, boolean combineVerbs) throws IOException {
        // Extracted relation must contain at lease one VP chunk tag
        addMapper(new VerbFilter());

        // Combine separated verbs
        if (combineVerbs) {
            addMapper(new SeparatedVerbMapper());
        }

        // Add lexical and syntactic constraints on the relation.
        if (useLexSynConstraints) {
            addLexicalAndSyntacticConstraints();
        }

        // The relation should have a minimum number of distinct arguments in a
        // large corpus
        if (minFreq > 0) {
            addMapper(new ReVerbRelationDictionaryFilter(minFreq));
        }
        // Overlapping relations should be merged together
        if (mergeOverlapRels) {
            addMapper(new MergeOverlappingMapper());
        }
    }

    private void addLexicalAndSyntacticConstraints() {
        /*
         * The relation shouldn't just be a single character. This usually
         * happens due to errors in the various NLP tools (sentence detector,
         * tokenizer, POS tagger, chunker).
         */
        addMapper(new FilterMapper<ChunkedRelationExtraction>() {
            public boolean doFilter(ChunkedRelationExtraction rel) {
                return rel.getLength() != 1 || rel.getToken(0).length() > 1;
            }
        });

        // These pos tags and tokens cannot appear in the relation
        StopListFilter relStopList = new StopListFilter();
        relStopList.addStopPosTag("KOUS");
        relStopList.addStopPosTag("$,");
        addMapper(relStopList);

        // The POS tag of the first verb in the relation cannot be VVPP, VAPP, VMPP
        addMapper(new FilterMapper<ChunkedRelationExtraction>() {
            public boolean doFilter(ChunkedRelationExtraction rel) {
                ChunkedSentence sent = rel.getSentence();
                int start = rel.getStart();
                int length = rel.getLength();
                for (int i = start; i < start + length; i++) {
                    String posTag = sent.getPosTags().get(i);

                    if (posTag.startsWith("V")) {
                        return !posTag.equals("VVPP") && !posTag.equals("VAPP") && !posTag
                            .equals("VMPP");
                    }
                }
                return true;
            }
        });

        // The previous tag can't be a "zu"
        addMapper(new FilterMapper<ChunkedRelationExtraction>() {
            public boolean doFilter(ChunkedRelationExtraction rel) {
                int s = rel.getStart();
                if (s == 0) {
                    return true;
                } else {
                    String posTag = rel.getSentence().getPosTag(s - 1);
                    return !posTag.equals("PKTZU");
                }
            }
        });
    }
}
