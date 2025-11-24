package com.crazedout.jsun.test;

public class Person {

    private final String name;
    private final String instrument;

    Person(String name, String instrument){
        this.name=name;
        this.instrument=instrument;
    }

    public String getName(){
        return this.name;
    }

    public String getInstrument(){
        return this.instrument;
    }

}
