package edu.washington.cs.knowitall.extractor.chunking.conf;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import edu.washington.cs.knowitall.extractor.chunking.ReVerbIExtractor;
import edu.washington.cs.knowitall.nlp.chunking.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.chunking.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.chunking.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.util.DefaultObjects;

/**
 * Used to create a labeled data set using the ReVerb extractions from a given file. See the
 * documentation for <code>LabeledBinaryExtractionReader</code> for details on the output format.
 *
 * @author afader
 */
public class ReVerbDataSetCreator {

    /**
     * Runs ReVerb on the given file (the first argument), and writes the output to the given target
     * file (the second argument). If the first argument is "-", then reads the data from standard
     * input.
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: ReVerbDataSetCreator input output");
            return;
        }

        BufferedReader reader;
        if (args[0].equals("-")) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } else {
            reader = new BufferedReader(new FileReader(args[0]));
        }

        ChunkedSentenceReader sentReader = DefaultObjects
            .getDefaultSentenceReader(reader);

        OutputStream outStream = new FileOutputStream(args[1]);
        LabeledBinaryExtractionWriter out = new LabeledBinaryExtractionWriter(
            outStream);

        ReVerbIExtractor extractor = new ReVerbIExtractor();

        for (ChunkedSentence sent : sentReader.getSentences()) {
            for (ChunkedBinaryExtraction extr : extractor.extract(sent)) {
                LabeledBinaryExtraction extrLabeled = new LabeledBinaryExtraction(
                    extr, 0);
                out.writeExtraction(extrLabeled);
            }
        }

    }

}