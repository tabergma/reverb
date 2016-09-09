package edu.washington.cs.knowitall.extractor.chunking;

import edu.washington.cs.knowitall.extractor.Extractor;
import edu.washington.cs.knowitall.util.DefaultObjects;
import opennlp.tools.sentdetect.SentenceDetector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * An <code>Extractor</code> object that extracts <code>String</code> sentences from a
 * <code>String</code>. Is backed by an OpenNLP <code>SentenceDetector</code> object.
 *
 * @author afader
 */
public class SentenceExtractor extends Extractor<String, String> {

    private SentenceDetector detector;

    /**
     * Constructs a new <code>SentenceExtractor</code> object using the default OpenNLP
     * <code>SentenceDetector</code> object, as returned by <code>DefaultObjects.getDefaultSentenceDetector()</code>.
     * @throws IOException if the sentence detector could not be loaded
     */
    public SentenceExtractor() throws IOException {
        this.detector = DefaultObjects.getDefaultSentenceDetector();
    }

    /**
     * Constructs a new <code>SentenceExtractor</code> object using the given OpenNLP
     * <code>SentenceDetector</code> object.
     * @param detector the sentence detector
     */
    public SentenceExtractor(SentenceDetector detector) {
        this.detector = detector;
    }

    /**
     * @return the OpenNLP <code>SentenceDetector</code> object.
     */
    public SentenceDetector getSentenceDetector() {
        return detector;
    }

    /**
     * Runs the OpenNLP <code>SentenceDetector</code> object on the given <code>String</code>
     * source, and returns an <code>Iterable</code> object over the detected sentences.
     * @param source the string for detecting sentences
     */
    protected Collection<String> extractCandidates(String source) {
        return Arrays.asList(detector.sentDetect(source));
    }
}
