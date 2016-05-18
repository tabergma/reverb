package edu.washington.cs.knowitall.extractor.chunking.conf.featureset;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.chunking.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.chunking.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.chunking.ChunkedBinaryExtraction;

/**
 * Parent class for any feature that only considers the token layer.
 *
 * @author Rob
 */
public abstract class TokenFeature extends ExtractionFeature {

    private Set<String> tokens;

    public TokenFeature(String... givenTokens) {
        this(Arrays.asList(givenTokens));
    }

    public TokenFeature(Collection<String> givenTokens) {
        this.tokens = new HashSet<String>();
        this.tokens.addAll(givenTokens);
    }

    @Override
    protected abstract Range rangeToExamine(ChunkedBinaryExtraction cbe);

    @Override
    protected boolean testAtIndex(Integer index, ChunkedSentence sentence) {

        String token = sentence.getToken(index);
        token = lemmatizer.lemmatize(token);
        token = token.toLowerCase();
        return tokens.contains(token);
    }

    /**
     * Get a feature that fires if any element of tokens is present within arg2.
     */
    public static ExtractionFeature withinArg2(String... tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getArgument2().getRange();
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present within the relation
     */
    public static ExtractionFeature withinRel(String... tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getRelation().getRange();
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present at the index immediately before
     * arg1
     */
    public static ExtractionFeature rightBeforeArg1(Collection<String> tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg1 = cbe.getArgument1();
                int index = arg1.getStart() - 1;
                if (index < 0 || index > arg1.getSentence().getLength()) {
                    return Range.EMPTY;
                } else {
                    return Range.fromInterval(index, index + 1);
                }
            }
        };
    }

    public static ExtractionFeature rightBeforeArg1(String... tokens) {
        return rightBeforeArg1(Arrays.asList(tokens));
    }

    /**
     * Get a feature that fires if any element of tokens is present at the index of the relation's
     * head verb.
     */
    public static ExtractionFeature relationHeadVerb(Collection<String> tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                Integer index = indexOfHeadVerb(cbe.getRelation(), false);
                if (index == null) {
                    return Range.EMPTY;
                } else {
                    return Range.fromInterval(index, index + 1);
                }
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present at the index immediately after
     * arg2.
     */
    public static ExtractionFeature rightAfterArg2(String... tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg2 = cbe.getArgument2();
                int index = arg2.getStart() + arg2.getLength();
                if (index < 0 || index >= arg2.getSentence().getLength()) {
                    return Range.EMPTY;
                } else {
                    return Range.fromInterval(index, index + 1);
                }
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present at any index prior to arg 1.
     */
    public static ExtractionFeature anywhereBeforeArg1(Collection<String> tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg1 = cbe.getArgument1();
                int index = arg1.getStart() - 1;
                if (index < 0 || index >= arg1.getSentence().getLength()) {
                    return Range.EMPTY;
                } else {
                    return Range.fromInterval(0, index + 1);
                }
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present at any index after arg2.
     */
    public static ExtractionFeature anywhereAfterArg2(Collection<String> tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg2 = cbe.getArgument2();
                int index = arg2.getStart() + arg2.getLength();
                if (index < 0 || index >= arg2.getSentence().getLength()) {
                    return Range.EMPTY;
                } else {
                    return Range.fromInterval(index, cbe.getSentence()
                        .getLength());
                }
            }
        };
    }

    /**
     * Get a feature that fires if any element of tokens is present in a single token relation
     * (returns empty range if relation is longer than one token).
     */
    public static ExtractionFeature relSingleToken(Collection<String> tokens) {
        return new TokenFeature(tokens) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {

                Range range = cbe.getRelation().getRange();
                if (range.getLength() == 1) {
                    return range;
                } else {
                    return Range.EMPTY;
                }
            }

        };
    }

}