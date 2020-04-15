package bowling;

import com.github.javafaker.Faker;
import lombok.experimental.Delegate;

import java.util.Random;
import java.util.UUID;

public class ARandom {
    @Delegate
    private Random random;

    @Delegate
    private Faker faker;


    public ARandom() {
        long seed = System.currentTimeMillis();
        random = new Random(seed);
        faker = Faker.instance(random);
    }

    public static final ARandom aRandom = new ARandom();

    public UUID uuid() {
        return UUID.randomUUID();
    }

    public int intBetween(int min, int max) {
        return aRandom.number().numberBetween(min, max);
    }
}
