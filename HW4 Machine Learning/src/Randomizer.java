import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Randomizer {

	public static void main(String[] args) {
		// Math.random() returns a value between 0.0 and 1.0
		// so it is heads or tails 50% of the time

		double numCorrect = 0;
		double count = 0;

		File f = new File("random.census.test");
		Scanner input;
		try {
			input = new Scanner(f);
			while (input.hasNextLine()) {
				count++;
				String arr[] = input.nextLine().split(" ");

				// System.out.print("Actual: " + arr[arr.length - 1].trim() +
				// " ");

				if (coinFlip().equals(arr[arr.length - 1].trim())) {
					numCorrect++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("NumCorrect: " + numCorrect);
		System.out.println(numCorrect / count);

	}

	public static String coinFlip() {
		if (Math.random() < 0.5) {
			// System.out.println("Guess: <=50K");
			return "<=50K";
		} else {
			// System.out.println("Guess: >50K");
			return ">50K";
		}
	}

}
