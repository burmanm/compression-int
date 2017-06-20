package fi.iki.yak.compression.integer;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generate Zipf (power law) distributed sets for testing compression algorithms
 *
 * @author Michael Burman
 */
public class ZipfDistribution {

    private ThreadLocalRandom random;

    private double alpha;
    private int elements;
    private double constant;

    public ZipfDistribution(double alpha, int elements) {
        random = ThreadLocalRandom.current();
        this.alpha = alpha;
        this.elements = elements;
        constant = initializeZipf(alpha, elements);
    }

    private int next() {
        // Map z to the value
        double sumOfProbabilities = 0;
        double zipfValue = 0;
        double z = 0;

        while(z == 0) {
            z = random.nextDouble();
        }

        for (int i = 1; i <= elements; i++) {
            sumOfProbabilities = sumOfProbabilities + constant / Math.pow((double) i, alpha);

            if (sumOfProbabilities >= z) {
                zipfValue = i;
                break;
            }
        }

        return (int) (zipfValue - 1); // Between n and 1
    }

    private double initializeZipf(double alpha, int elements) {
        double norm = 0;
        for(int i = 1; i <= elements; i++) {
            norm = norm + (1.0 / Math.pow((double) i, alpha));
        }

        norm = 1.0 / norm;

        return norm;
    }

    public IntStream stream() {
        return IntStream.generate(this::next);
    }

    public static void main(String[] args) {
        ZipfDistribution zipf = new ZipfDistribution(1.0, 10);
        zipf.stream().limit(100)
                .sorted()
                .forEach(System.out::println);
    }
}
