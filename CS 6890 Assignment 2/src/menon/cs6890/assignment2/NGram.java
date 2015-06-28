package menon.cs6890.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NGram {
	
	private Collection<String> corpusText;
	private Map<String, Integer> bigramCounts = new HashMap<String, Integer>();
	private Map<String, Integer> trigramCounts = new HashMap<String, Integer>();
	private Map<String, Integer> wordCounts = new HashMap<String, Integer>();
	
	private static final String BEGINNING_MARKER = "XXXXXXXXXXXXX ";
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.err.println("File path must be provided as a parameter.");
			System.exit(0);
		}
		NGram nGramInstance = new NGram(args[0]);
		nGramInstance.addBeginningMarkers();
		nGramInstance.removeWhitespaceCharacters();
		nGramInstance.computeWordCounts();
		nGramInstance.computeBigrams();
		nGramInstance.computeTrigrams();

		double digramScore = nGramInstance.bigramSeqProb("Plunging at once into the sacred Fire");
		System.out.println("Digram probability of \"Plunging at once into the sacred Fire\" is " + digramScore);

		digramScore = nGramInstance.bigramSeqProb("One flew over the cuckoos nest");
		System.out.println("Digram probability of \"One flew over the cuckoos nest\" is " + digramScore);
		
		double trigramScore = nGramInstance.trigramSeqProb("Plunging at once into the sacred Fire");
		System.out.println("Trigram probability of \"Plunging at once into the sacred Fire\" is " + trigramScore);
		
		trigramScore = nGramInstance.trigramSeqProb("One flew over the cuckoos nest");
		System.out.println("Trigram probability of \"One flew over the cuckoos nest\" is " + trigramScore);
	}
	
	public NGram(String filePath) {
		
		//File path should be present and must be valid
		if (filePath == null || filePath.length() == 0) {
			System.err.println("File path must be provided as a parameter.");
			System.exit(0);
		}
		
		File corpus = new File(filePath);
		if (!corpus.isFile()) {
			System.err.println(filePath + " was not found.");
			System.exit(0);
		}
		
		this.corpusText = getTextLines(corpus);
	}
	
	//Compute bigram counts
	public void computeBigrams() {
		
		String word1 = null, word2 = null, bigram = null;
		Integer count = null;
		for (String token : this.corpusText) {
			token = token.replaceAll("[^a-zA-Z]", "");
			if (token.length() > 0) {
				if ((word1 == null && word2 == null) || (word1.contains(BEGINNING_MARKER.trim()) && token.contains(BEGINNING_MARKER.trim()))) {
					word1 = token;
				} else {
					word2 = token;
					bigram = word1 + " " + word2;
					if (bigramCounts.containsKey(bigram)) {
						count = bigramCounts.get(bigram);
						count = Integer.valueOf(count.intValue() + 1);
						bigramCounts.put(bigram, count);
					} else {
						bigramCounts.put(bigram, Integer.valueOf(1));
					}
					word1 = word2;
					word2 = null;
				}
			}
		}
		
	}
		
	//Compute trigram counts
	public void computeTrigrams() {
		
		String word1 = null, word2 = null, word3 = null, trigram = null;
		Integer count = null;
		for (String token : this.corpusText) {
			token = token.replaceAll("[^a-zA-Z]", "");
			if (token.length() > 0) {
				if (word1 == null && word2 == null && word3 == null) {
					word1 = token;
				} else if (word2 == null && word3 == null) {
					word2 = token;
				} else {
					word3 = token;
					trigram = word1 + " " + word2 + " " + word3;
					if (trigramCounts.containsKey(trigram)) {
						count = trigramCounts.get(trigram);
						count = Integer.valueOf(count.intValue() + 1);
						trigramCounts.put(trigram, count);
					} else {
						trigramCounts.put(trigram, Integer.valueOf(1));
					}
					word1 = word2;
					word2 = word3;
					word3 = null;
				}
			}
		}
		
	}
	
	public double bigramSeqProb(String words) {
		
		String word1 = null, word2 = null, bigram = null;
		double word1Count = 0, bigramCount = 0, sumOfProbablityLogs = 0;
		
		if (words == null || words.length() == 0) {
			return 1.0;
		}
		
		String[] tokens = words.toLowerCase().split("\\s+");
		sumOfProbablityLogs = 0;
		for (String word : tokens) {
			if (word1 == null && word2 == null) {
				word1 = word;
			} else {
				word2 = word;
				bigram = word1 + " " + word2;
				bigramCount = 0;
				if (bigramCounts.containsKey(bigram)) {
					bigramCount = bigramCounts.get(bigram).intValue();
				}
				//Smoothing
				bigramCount += 1; 
				word1Count = 0;
				if (wordCounts.containsKey(word1)) {
					word1Count = wordCounts.get(word1).intValue();
				}
				//Smoothing
				word1Count += wordCounts.size();
				sumOfProbablityLogs += Math.log10(bigramCount / word1Count);
				word1 = word2;
				word2 = null;
			}
		}
		return Math.pow(10, sumOfProbablityLogs);
	}
	
	public double trigramSeqProb(String words) {
		
		String word1 = null, word2 = null, word3 = null, prefix = null, trigram = null;
		double prefixCount = 0, trigramCount = 0, sumOfProbablityLogs = 0;
		
		if (words == null || words.length() == 0) {
			return 1.0;
		}
		
		String[] tokens = words.toLowerCase().split("\\s+");
		sumOfProbablityLogs = 0;
		for (String word : tokens) {
			if (word1 == null && word2 == null && word3 == null) {
				word1 = word;
			} else if (word2 == null && word3 == null) {
				word2 = word;
			} else {	
				word3 = word;
				trigram = word1 + " " + word2 + " " + word3;
				trigramCount = 0;
				if (trigramCounts.containsKey(trigram)) {
					trigramCount = trigramCounts.get(trigram).intValue();
				}
				//Smoothing
				trigramCount += 1; 
				prefixCount = 0;
				prefix = word1 + " " + word2;
				if (bigramCounts.containsKey(prefix)) {
					prefixCount = bigramCounts.get(prefix).intValue();
				}
				//Smoothing
				prefixCount += wordCounts.size();
				sumOfProbablityLogs += Math.log10(trigramCount / prefixCount);
				word1 = word2;
				word2 = word3;
				word3 = null;
			}
		}
		return Math.pow(10, sumOfProbablityLogs);

	}	
	
	//The method will return a collection of lines in the file passed as parameter
	private Collection<String> getTextLines(File fileName) {

		Collection<String> textLines = new ArrayList<String>();
		try {
			BufferedReader textFileReader = new BufferedReader(new FileReader(fileName));
			String textLine = textFileReader.readLine();
			while(textLine != null) {
				textLines.add(textLine.toLowerCase());
				textLine = textFileReader.readLine();
			}
			textFileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return textLines;
	}
	
	//Add sentence beginning markers to corpus text
	private void addBeginningMarkers() {
		
		boolean firstTime = true;
		Collection<String> corpusTextWithMarkers = new ArrayList<String>();
		for (String textLine : this.corpusText) {
			if (firstTime) {
				firstTime = false;
				textLine = BEGINNING_MARKER + BEGINNING_MARKER + textLine;
			}
			textLine = textLine.replaceAll("\\.", " " + BEGINNING_MARKER + BEGINNING_MARKER);
			corpusTextWithMarkers.add(textLine);
		}
		this.corpusText = corpusTextWithMarkers;
	}
	
	//Remove whitespace characters
	private void removeWhitespaceCharacters() {
		Collection<String> corpusTextWithoutWhitespaces = new ArrayList<String>();
		String[] tokens = null;
		for (String textLine : this.corpusText) {
			tokens = textLine.split("[\\s+]");
			for (String word : tokens) {
				if (word.trim().length() > 0) {
					corpusTextWithoutWhitespaces.add(word);
				}
			}
		}
		this.corpusText = corpusTextWithoutWhitespaces;
	}
	
	//Compute word counts
	private void computeWordCounts() {
		
		Integer count = null;
		for (String token : this.corpusText) {
			token = token.replaceAll("[^a-zA-Z]", "");
			if (!token.contains(BEGINNING_MARKER.trim()) && token.length() > 0) {
				if (wordCounts.containsKey(token)) {
					count = wordCounts.get(token);
					count = Integer.valueOf(count.intValue() + 1);
					wordCounts.put(token, count);
				} else {
					wordCounts.put(token, Integer.valueOf(1));
				}
			}
		}
	}
}