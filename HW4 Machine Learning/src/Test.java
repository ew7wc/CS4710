
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NaiveBayes nb = new NaiveBayes("census.names");
		nb.train("census.train");
		nb.makePredictions("census.test1");
	}

}
