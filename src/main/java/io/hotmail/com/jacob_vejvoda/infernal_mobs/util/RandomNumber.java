package io.hotmail.com.jacob_vejvoda.infernal_mobs.util;

import java.util.Random;

public final class RandomNumber {

    private static final Random RANDOM = new Random();

    public static int generate(int bound) {
        return generate(bound, false);
    }

    public static int generate(int bound, boolean plusOne) {
        var num = 0 < bound ? RANDOM.nextInt(bound) : 0;
        return plusOne ? num + 1 : num;
    }

    public static int generate(int start, int end) {
        var dif = end - start;
        return RANDOM.nextInt(dif) + start;
    }

    public static boolean doLottery(int chance) {
        return chance == 1 || RANDOM.nextInt(chance) == 0;
    }
}
