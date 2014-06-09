package com.handwin.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

/**
 * User: roger
 * Date: 13-12-18 上午11:06
 */
public class Randoms {
    private static SecureRandom random = null;
    static {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    public static int randomSelect(int[] values) {
        return values[randomInt(values.length)];
    }



    public static int randomSelect(List<Integer> values) {
        return values.get(randomInt(values.size()));
    }

    public static void main(String[] args) {
        int w = 1136 , h= 640;
        System.out.println(randomInt(w) + "-" + randomInt(h));
        System.out.println(randomInt(w) + "-" + randomInt(h));
//        int[] values = new int[]{1,2,3,4,5,6,7,8};
//
//        Map<Integer, Integer> counter = new HashMap<Integer,Integer>();
//
//        for(int i = 0;i < 500; i++) {
//            int v = randomSelect(values);
//            if(counter.containsKey(v)) {
//                counter.put(v, counter.get(v) + 1);
//            } else {
//                counter.put(v, 1);
//            }
//        }
//
//        for(Integer k : counter.keySet()) {
//            Integer count = counter.get(k);
//            if(count == 0) {
//                System.out.println(k + " occurs 0");
//            } else {
//                System.out.println(k + " " + (float)count / 5 + "%");
//
//            }
//
//        }
    }



}
