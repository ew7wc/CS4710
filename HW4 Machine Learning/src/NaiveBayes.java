import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class NaiveBayes extends Classifier {

	HashMap<String, HashMap<String, double[]>> categorical = new HashMap<String, HashMap<String, double[]>>();
	HashMap<String, double[]> numeric = new HashMap<String, double[]>();
	String[] header = { "age", "workclass", "education", "education-num",
			"marital-status", "occupation", "relationship", "race", "sex",
			"capital-gain", "capital-loss", "hours-per-week", "native-country" };

	HashMap<String, Integer> headerPosition = new HashMap<String, Integer>();
	// Stored for the smoothing factor.
	HashMap<String, Integer> featurePossibleVals = new HashMap<String, Integer>();

	double totalData = 0;
	double less50 = 0;
	double greater50 = 0;
	double numCorrect = 0;

	String trainingFile = "";
	double smoothingFactor = 1;

	public NaiveBayes(String namesFilepath) {
		super(namesFilepath);
		readCensusNames(namesFilepath);
	}

	@Override
	public void train(String trainingDataFilpath) {
		readCensusTrain(trainingDataFilpath);
		trainingFile = trainingDataFilpath;

		for (String feature : categorical.keySet()) {
			for (String cat : categorical.get(feature).keySet()) {
				double[] probs = categorical.get(feature).get(cat);
				categorical.get(feature).get(cat)[0] = (probs[0] + smoothingFactor)
						/ (less50 + featurePossibleVals.get(feature)
								* smoothingFactor);
				categorical.get(feature).get(cat)[1] = (probs[1] + smoothingFactor)
						/ (greater50 + featurePossibleVals.get(feature)
								* smoothingFactor);
			}
		}

		for (String cat : numeric.keySet()) {
			calcMean(cat);
		}
		calcVariance();
	}
	
	public void readCensusTrain(String file) {
		File f = new File(file);
		Scanner input;

		try {
			input = new Scanner(f);

			while (input.hasNextLine()) {
				totalData++;
				String[] arr = input.nextLine().split(" ");
				if (arr[arr.length - 1].equals("<=50K")) {
					updateNumAndCategorical(true, arr);

				} else {
					updateNumAndCategorical(false, arr);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public void updateNumAndCategorical(boolean isLess50, String[] arr) {
		int catIndex = -1;
		int numIndex = -1;
		if (isLess50) {
			less50++;
			catIndex = 0;
			numIndex = 0;

		} else {
			greater50++;
			catIndex = 1;
			numIndex = 2;
		}

		numeric.get("age")[numIndex] += Double.parseDouble(arr[0]);
		categorical.get("workclass").get(arr[1])[catIndex]++;
		categorical.get("education").get(arr[2])[catIndex]++;
		numeric.get("education-num")[numIndex] += Double.parseDouble(arr[3]);
		categorical.get("marital-status").get(arr[4])[catIndex]++;
		categorical.get("occupation").get(arr[5])[catIndex]++;
		categorical.get("relationship").get(arr[6])[catIndex]++;
		categorical.get("race").get(arr[7])[catIndex]++;
		categorical.get("sex").get(arr[8])[catIndex]++;
		numeric.get("capital-gain")[numIndex] += Double.parseDouble(arr[9]);
		numeric.get("capital-loss")[numIndex] += Double.parseDouble(arr[10]);
		numeric.get("hours-per-week")[numIndex] += Double.parseDouble(arr[11]);
		categorical.get("native-country").get(arr[12])[catIndex]++;
	}

	public void calcMean(String cat) {
		double[] total = numeric.get(cat);
		total[0] = total[0] / less50;
		total[2] = total[2] / greater50;

	}	

	public void calcVariance() {

		// First value = less than 50, second value greater than 50 is the
		// summation term in the variance formula

		String[] numericFeatures = { "age", "education-num", "capital-gain",
				"capital-loss", "hours-per-week" };

		// int count = 0;
		HashMap<String, double[]> numFeatMap = new HashMap<String, double[]>();

		for (int i = 0; i < numericFeatures.length; i++) {
			numFeatMap.put(numericFeatures[i], new double[4]);
		}

		for (String s : numFeatMap.keySet()) {
			numFeatMap.get(s)[0] = numeric.get(s)[0];
			numFeatMap.get(s)[2] = numeric.get(s)[2];
		}

		File f = new File(trainingFile);
		Scanner input;
		try {
			input = new Scanner(f);

			while (input.hasNextLine()) {
				String[] arr = input.nextLine().split(" ");
				if (arr[arr.length - 1].equals("<=50K")) {

					for (String s : numFeatMap.keySet()) {

						numFeatMap.get(s)[1] = numFeatMap.get(s)[1]
								+ Math.pow(
										(Double.parseDouble(arr[headerPosition
												.get(s)]) - numFeatMap.get(s)[0]),
										2);

					}

				} else {
					for (String s : numFeatMap.keySet()) {
						numFeatMap.get(s)[3] = numFeatMap.get(s)[3]
								+ Math.pow(
										(Double.parseDouble(arr[headerPosition
												.get(s)]) - numFeatMap.get(s)[2]),
										2);
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (String s : numFeatMap.keySet()) {
			numFeatMap.get(s)[1] /= less50;
			numFeatMap.get(s)[3] /= greater50;
		}

		for (String s : numeric.keySet()) {
			numeric.get(s)[1] = numFeatMap.get(s)[1];
			numeric.get(s)[3] = numFeatMap.get(s)[3];
		}
	}

	@Override
	public void makePredictions(String testDataFilepath) {

		File f = new File(testDataFilepath);
		Scanner input;
		double probLess50 = 1;
		double probGreater50 = 1;
		try {

			input = new Scanner(f);
			while (input.hasNextLine()) {

				String arr[] = input.nextLine().split(" ");

				double onlyCategoricalR = 1;
				for (int i = 0; i < arr.length - 1; i++) {
					if (i == 0 || i==3 || i==9 || i==10 || i == 11) {
						// Call prob den funct
						double probL = probDensity(header[i],
								Double.parseDouble(arr[i]), true);
						;
						double probG = probDensity(header[i],
								Double.parseDouble(arr[i]), false);

						probLess50 *= probL;
						probGreater50 *= probG;

					} 
					
					//DECIDED TO INCLUDE ALL FEATURES
//					else if () {
//						// Categories not included in the calculation
//						// cap-gain (9), cap-loss (10)
//						// Features eliminated: *education-num (3),
//						// *relationship (6),
//						// native country (13), work class (1), race (7)
//						continue;
//					} 
					else {

						probLess50 *= categorical.get(header[i]).get(arr[i])[0];
						onlyCategoricalR *= categorical.get(header[i]).get(
								arr[i])[1];
						probGreater50 *= categorical.get(header[i]).get(arr[i])[1];

					}
				}
				// Multiply by P(<=50K) and P(>50K) which should be
				// numLess50/total or greater50/total
				probLess50 = (less50 / totalData) * probLess50;
				probGreater50 = (greater50 / totalData) * probGreater50;

				// Adding an education weight, the higher your education level,
				// the more likely you are to make over 50k.
				// education number is spot 3, max value is 15.

				// Calculating Weights
				double maxEduNum = 15;
				double edu_Num = Double.parseDouble(arr[3]);
				double w_less50 = (15 - edu_Num + 1) / (1 + maxEduNum);
				double w_greater50 = (edu_Num + 1) / (1 + maxEduNum);

				probLess50 *= w_less50;
				probGreater50 *= w_greater50;

				// Adding a marital status weight. If you are married it seems
				// like
				// you have a ~70% chance of making more than 50k
				if (arr[4].trim().equals("Married-civ-spouse")) {
					probLess50 *= .333;
					probGreater50 *= .666;
				}
				//Seems more likely to make >50K if 
				//education level is Bachelors
				if (arr[2].trim().equals("Bachelors")) {
					probLess50 *= .333;
					probGreater50 *= .666;
				}
				//Seems more likely to make >50K if occupation is
				//Exec-managerial or Prof-specialty
				if (arr[5].trim().equals("Exec-managerial") || arr[5].trim().equals("Prof-specialty")) {
					probLess50 *= .3;
					probGreater50 *= .7;
				}

				if (probLess50 >= probGreater50) {

					System.out.println("<=50K");
					//FOR TESTING PURPOSES
//					if (arr[arr.length - 1].trim().equals("<=50K")) {
//						numCorrect++;
//					} else {
//						BigDecimal l_50 = new BigDecimal(probLess50);
//						l_50 = l_50.round(new MathContext(3));
//						double rounded = l_50.doubleValue();
//
//						BigDecimal g_50 = new BigDecimal(probGreater50);
//						g_50 = g_50.round(new MathContext(3));
//						double rounded2 = g_50.doubleValue();
//
////						System.out.print("ACTUAL: " + arr[arr.length - 1]
////								+ "\tProbs: <=50K: " + rounded + " ");
////						System.out.println(">50K: " + rounded2 + "\tDataset: "
////								+ Arrays.toString(arr));
//					}
				} else if (probLess50 < probGreater50) {

					System.out.println(">50K");
					
					//FOR TESTING PURPOSES
//					if (arr[arr.length - 1].trim().equals(">50K")) {
//						numCorrect++;
//					} else {
//						System.out.println(">50K");
//						BigDecimal l_50 = new BigDecimal(probLess50);
//						l_50 = l_50.round(new MathContext(3));
//						double rounded = l_50.doubleValue();
//
//						BigDecimal g_50 = new BigDecimal(probGreater50);
//						g_50 = g_50.round(new MathContext(3));
//						double rounded2 = g_50.doubleValue();
//
////						System.out.print("ACTUAL: " + arr[arr.length - 1]
////								+ "\tProbs: <=50K: " + rounded + " ");
////						System.out.println(">50K: " + rounded2 + "\tDataset: "
////								+ Arrays.toString(arr));
//					}
				}

				probLess50 = 1;
				probGreater50 = 1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//System.out.println(numCorrect / 999);

	}

	public double probDensity(String category, double val, boolean less50) {
		double mean = 0;
		double variance = 0;

		if (less50) {
			mean = numeric.get(category)[0];
			variance = numeric.get(category)[1];

		} else {
			mean = numeric.get(category)[2];
			variance = numeric.get(category)[3];

		}
		// System.out.println("CATEGORY: " + category + " MEAN: " + mean +
		// " VARIANCE: " + variance + " VALUE: " + val);
		double num = Math.pow(val - mean, 2) / (2 * variance);
		double denom = Math.sqrt(2 * Math.PI * variance);
		// System.out.println("\tP(V|C): " + Math.exp(-num)/denom);
		return Math.exp(-num) / denom;
	}

	public void readCensusNames(String file) {
		File f = new File(file);
		Scanner input;
		int count = 0;
		try {
			input = new Scanner(f);
			String ouput_cat = input.nextLine();
			input.nextLine();
			while (input.hasNextLine()) {
				String[] s = input.nextLine().split(" ");
				// System.out.println(Arrays.toString(s));
				// System.out.println(s[0]);
				headerPosition.put(s[0], count);
				count++;
				if (s[1].equals("numeric")) {
					double[] tmp = new double[4];
					numeric.put(s[0], tmp);
				} else {
					HashMap<String, double[]> tmp = new HashMap<String, double[]>();
					for (int i = 1; i < s.length; i++) {
						double[] arrTmp = new double[2];
						tmp.put(s[i], arrTmp);
					}
					categorical.put(s[0], tmp);

					featurePossibleVals.put(s[0], s.length - 1);
				}

				// System.out.println(Arrays.toString(s));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	


	// THE FOLLOWING METHODS WERE FOR TESTING PURPOSES (on sections of the files)
	public void train(String trainingDataFilpath, int startLine, int endLine) {
		readCensusTrain(trainingDataFilpath, startLine, endLine);
		trainingFile = trainingDataFilpath;

		for (String feature : categorical.keySet()) {
			for (String cat : categorical.get(feature).keySet()) {
				double[] probs = categorical.get(feature).get(cat);
				categorical.get(feature).get(cat)[0] = (probs[0] + smoothingFactor)
						/ (less50 + featurePossibleVals.get(feature)
								* smoothingFactor);
				categorical.get(feature).get(cat)[1] = (probs[1] + smoothingFactor)
						/ (greater50 + featurePossibleVals.get(feature)
								* smoothingFactor);
			}
		}

		for (String cat : numeric.keySet()) {
			calcMean(cat);
		}
		calcVariance(startLine, endLine);

		// Print out calculated probs
		/*
		 * for (String feature: categorical.keySet()) { for (String cat:
		 * categorical.get(feature).keySet()) { double[] probs =
		 * categorical.get(feature).get(cat);
		 * 
		 * System.out.println(feature + " " + cat + " " +
		 * Arrays.toString(probs)); } System.out.println(); }
		 */
	}

	public void readCensusTrain(String file, int startingLine, int endingLine) {
		File f = new File(file);
		Scanner input;
		int count = 0;
		try {
			input = new Scanner(f);
			while (input.hasNextLine()) {
				count++;
				if (count >= startingLine && count <= endingLine) {
					totalData++;
					String[] arr = input.nextLine().split(" ");
					if (arr[arr.length - 1].equals("<=50K")) {
						updateNumAndCategorical(true, arr);

					} else {
						updateNumAndCategorical(false, arr);

					}
				} else if (count > endingLine) {
					break;
				} else {
					input.nextLine();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public void makePredictions(String testDataFilepath, int startingLine,
			int endingLine) {

		int count = 0;
		File f = new File(testDataFilepath);
		Scanner input;
		double probLess50 = 1;
		double probGreater50 = 1;
		try {

			input = new Scanner(f);
			while (input.hasNextLine()) {
				count++;

				if (count >= startingLine && count <= endingLine) {
					String arr[] = input.nextLine().split(" ");

					double onlyCategoricalR = 1;
					for (int i = 0; i < arr.length - 1; i++) {
						if (i == 0 || i==3 || i==9 || i==10 || i == 11) {
							// Call prob den funct
							double probL = probDensity(header[i],
									Double.parseDouble(arr[i]), true);
							;
							double probG = probDensity(header[i],
									Double.parseDouble(arr[i]), false);

							probLess50 *= probL;
							probGreater50 *= probG;

						} 
						else if (i==4) {
							// Categories not included the calculation
							// cap-gain (9), cap-loss (10)
							// education-num (3), relationship (6),
							// native country (13), work class (1), race (7)
							continue;
						} 
						else {

							probLess50 *= categorical.get(header[i])
									.get(arr[i])[0];
							onlyCategoricalR *= categorical.get(header[i]).get(
									arr[i])[1];
							probGreater50 *= categorical.get(header[i]).get(
									arr[i])[1];

						}
					}
					// Multiply by P(<=50K) and P(>50K) which should be
					// numLess50/total or greater50/total
					probLess50 = (less50 / totalData) * probLess50;
					probGreater50 = (greater50 / totalData) * probGreater50;

					// Adding an education weight, the higher your education
					// level,
					// the more likely you are to make over 50k.
					// education number is spot 3, max value is 15.

					// Calculating Weights
//					double maxEduNum = 15;
//					double edu_Num = Double.parseDouble(arr[3]);
//					double w_less50 = (15 - edu_Num + 1) / (1 + maxEduNum);
//					double w_greater50 = (edu_Num + 1) / (1 + maxEduNum);
//
//					probLess50 *= w_less50;
//					probGreater50 *= w_greater50;
//
//					// Adding a marital status weight. If you are married it
//					// seems like
//					// you have a ~70% chance of making more than 50k
//					if (arr[4].trim().equals("Married-civ-spouse")) {
//						probLess50 *= .333;
//						probGreater50 *= .666;
//					}
//					if (arr[2].trim().equals("Bachelors")) {
//						probLess50 *= .333;
//						probGreater50 *= .666;
//					}
//					if (arr[5].trim().equals("Exec-managerial") || arr[5].trim().equals("Prof-specialty")) {
//						probLess50 *= .3;
//						probGreater50 *= .7;
//					}

					if (probLess50 >= probGreater50) {

						if (arr[arr.length - 1].trim().equals("<=50K")) {
							numCorrect++;
						} else {
							System.out.print("GUESS <=50K \t");
							BigDecimal l_50 = new BigDecimal(probLess50);
							l_50 = l_50.round(new MathContext(3));
							double rounded = l_50.doubleValue();

							BigDecimal g_50 = new BigDecimal(probGreater50);
							g_50 = g_50.round(new MathContext(3));
							double rounded2 = g_50.doubleValue();

							System.out.print("ACTUAL: " + arr[arr.length - 1]
									+ "\tProbs: <=50K: " + rounded + " ");
							System.out.println(">50K: " + rounded2
									+ "\tDataset: " + Arrays.toString(arr));
						}
					} else if (probLess50 < probGreater50) {

						if (arr[arr.length - 1].trim().equals(">50K")) {
							numCorrect++;
						} else {
							System.out.print("GUESS >50K  \t");
							BigDecimal l_50 = new BigDecimal(probLess50);
							l_50 = l_50.round(new MathContext(3));
							double rounded = l_50.doubleValue();

							BigDecimal g_50 = new BigDecimal(probGreater50);
							g_50 = g_50.round(new MathContext(3));
							double rounded2 = g_50.doubleValue();

							System.out.print("ACTUAL: " + arr[arr.length - 1]
									+ "\tProbs: <=50K: " + rounded + " ");
							System.out.println(">50K: " + rounded2
									+ "\tDataset: " + Arrays.toString(arr));
						}
					}

					probLess50 = 1;
					probGreater50 = 1;
				} else if (count > endingLine) {
					break;
				} else {
					input.nextLine();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(numCorrect / (endingLine - startingLine + 1));
		System.out.println((endingLine - startingLine));

	}
	
	public void calcVariance(int startLine, int endLine) {

		// First value = less than 50, second value greater than 50 is the
		// summation term in the variance formula

		String[] numericFeatures = { "age", "education-num", "capital-gain",
				"capital-loss", "hours-per-week" };

		int count = 0;
		HashMap<String, double[]> numFeatMap = new HashMap<String, double[]>();

		for (int i = 0; i < numericFeatures.length; i++) {
			numFeatMap.put(numericFeatures[i], new double[4]);
		}

		for (String s : numFeatMap.keySet()) {
			numFeatMap.get(s)[0] = numeric.get(s)[0];
			numFeatMap.get(s)[2] = numeric.get(s)[2];
		}

		File f = new File(trainingFile);
		Scanner input;
		try {
			input = new Scanner(f);

			while (input.hasNextLine()) {
				count++;
				if (count >= startLine && count <= endLine) {
					// Look at spots 0, 3, 9, 10, 11 for numeric vals
					String[] arr = input.nextLine().split(" ");
					if (arr[arr.length - 1].equals("<=50K")) {

						for (String s : numFeatMap.keySet()) {

							numFeatMap.get(s)[1] = numFeatMap.get(s)[1]
									+ Math.pow((Double
											.parseDouble(arr[headerPosition
													.get(s)]) - numFeatMap
											.get(s)[0]), 2);

						}

					} else {
						for (String s : numFeatMap.keySet()) {
							numFeatMap.get(s)[3] = numFeatMap.get(s)[3]
									+ Math.pow((Double
											.parseDouble(arr[headerPosition
													.get(s)]) - numFeatMap
											.get(s)[2]), 2);
						}
					}
				} else if (count > endLine) {
					break;
				} else {
					input.nextLine();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (String s : numFeatMap.keySet()) {
			numFeatMap.get(s)[1] /= less50;
			numFeatMap.get(s)[3] /= greater50;
		}

		for (String s : numeric.keySet()) {
			numeric.get(s)[1] = numFeatMap.get(s)[1];
			numeric.get(s)[3] = numFeatMap.get(s)[3];
		}
	}
	
	public static void main(String[] args) {

		NaiveBayes nb = new NaiveBayes("census.names");
		//nb.train("census.train", 1, 1300);
		//nb.makePredictions("census.train", 1301, 1499);
		nb.train("census.train");
		nb.makePredictions("census.test1");

	}

}
