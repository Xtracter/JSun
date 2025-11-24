package com.crazedout.jsun.test;

import com.crazedout.jsun.JSun;

import java.util.Arrays;
import java.util.List;

public class Tutorials {

    public List<String> getNames(){
        String[] beat = {"John","Paul","George","Ringo"};
        return Arrays.asList(beat);
    }

    public List<Object> getValues(){
        Object[] beat = {"John","Paul","George",new JSun("Ringo","Drummer")};
        return Arrays.asList(beat);
    }
}
