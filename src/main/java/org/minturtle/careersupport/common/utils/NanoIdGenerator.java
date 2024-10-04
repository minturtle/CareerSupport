package org.minturtle.careersupport.common.utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Random;

public abstract class NanoIdGenerator {

    public static String createNanoId(int idSize){
        Random random = new Random();

        return NanoIdUtils.randomNanoId(random, NanoIdUtils.DEFAULT_ALPHABET,idSize);

    }

    public static String createNanoId(){
        return createNanoId(10);
    }

}