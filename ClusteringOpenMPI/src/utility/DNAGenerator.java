package utility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

public class DNAGenerator extends Generator {
	private static final int FIVE_SEC = 10000;	
	private static final int ARGS_LEN = 5;
	private static final String DNA_SEQ = "ACGT";

	private int numOfCluster = 3;
	private int numOfDNA = 20;
	private int lengthOfDNA = 10;
	private String fileName = "DNA.csv";
	private int maxVar = 8;

	private static TreeSet<String> centroid = new TreeSet<String>();
	private static HashSet<String> DNAs = new HashSet<String>();

	@Override
	protected void printUsage() {
		System.out.println("Default value used.");
		System.out.println("Usage : DNAGenerator [numOfClusters] [numOfDNA/cluster] [lengthOfDNA] [output filename] [max variance]");
		System.out.println("e.g.    DNAGenerator 3 20 10 DNA 8");
		System.out.println("        3 clusters, 20 DNA per cluster, length of DNA 10, max variance 8 data generated in DNA.csv");
	}

	@Override
	protected void parseArgs(String[] args) {
		numOfCluster = Integer.parseInt(args[0]);
		numOfDNA = Integer.parseInt(args[1]);
		lengthOfDNA = Integer.parseInt(args[2]);
		fileName = args[3] + ".csv";
		maxVar = Integer.valueOf(args[4]);
	}

	@Override
	protected void generateCentroid() {
		int count = 0;
		while (count < numOfCluster) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < lengthOfDNA; i++) {
				sb.append(DNA_SEQ.charAt(getUniform(0, 3)));
			}

			String randCentroid = sb.toString();
			if (!centroid.contains(randCentroid)) {
				centroid.add(randCentroid);
				count++;
			}
		}
	}

	@Override
	protected void generateData() {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			Iterator<String> itor = centroid.iterator();
			while (itor.hasNext()) {
				int count = 0;
				String mean = itor.next();
				int variance = getUniform(0, maxVar);

				while (count < numOfDNA) {
					HashSet<Integer> set = new HashSet<Integer>();
					while (set.size() < variance) {
						set.add(getUniform(0, lengthOfDNA));
					}

					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < lengthOfDNA; i++) {
						if (set.contains(i)) {
							char c = mean.charAt(i);
							String s = DNA_SEQ.replace(String.valueOf(c), "");
							sb.append(s.charAt(getUniform(0, s.length())));
						} else {
							sb.append(mean.charAt(i));
						}
					}

					String DNA = sb.toString();
					//if (!DNAs.contains(DNA)) {	// unique data
						DNAs.add(DNA);
						count++;
						bw.write(DNA);
						bw.newLine();
					//}
				}
			}

			bw.close();
			fos.close();
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) throws IOException {
		Reminder rmd = new Reminder(FIVE_SEC);
		DNAGenerator dna = new DNAGenerator();

		if (args.length != ARGS_LEN) {
			dna.printUsage();
		} else {
			dna.parseArgs(args);
		}

		dna.generateCentroid();
		dna.generateData();
		rmd.cancel();
		System.out.println("Data Generation Succeed!");
	}
}
