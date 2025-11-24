package com.crazedout.jsun.test;

import com.crazedout.jsun.JSun;
import org.junit.jupiter.api.Test;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestJSun {

    private static void out(String str){
        System.out.println(str);
    }

    @Test
    public void testDuplicateKeyJSun() {

        Tutorial tut = new Tutorial();
        tut.setValues(1,"Java One on One","Java book",1);
        JSun jsun = new JSun(tut);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            jsun.put("id",4);
        });
        String expectedMessage = "Duplicate key 'id'.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testNullsJSun() {
        Tutorial tut = new Tutorial();
        tut.setValues(1,"Java One on One","Java book", 2);
        JSun jsun = new JSun(tut);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            jsun.put(null,"Sven");
        });
        String expectedMessage = "Key may not be null.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testSimpleJSun() throws ScriptException {
        Tutorial tut = new Tutorial();
        tut.setValues(1,"Java One on One","Java book",1);
        JSun json = new JSun(tut);
        // Test that the JSunClass excludes work.
        assertFalse(json.toJsonString().contains("values"));
        json.eval();
    }

    @Test
    public void testJSun() throws ScriptException {
        Tutorials tut = new Tutorials();
        JSun json = new JSun(tut);
        String js = "var json = JSON.parse('" + json+"');";
        JSun.eval(js);
    }

    @Test
    public void testArrays() throws ScriptException {
        Person[] persons = {new Person("John","Guitar"), new Person("Paul","Bass")};
        (new JSun("Beat", persons)).eval();
    }

    @Test
    public void beatlesTest() throws ScriptException {

        List<Person> beatlesList = new ArrayList<>();
        beatlesList.add(new Person("John", "Guitar"));
        beatlesList.add(new Person("Paul", "Bass"));
        beatlesList.add(new Person("George", "Guitar"));
        beatlesList.add(new Person("Ringo", "Drums"));
        JSun js = new JSun(beatlesList);
        out(js.eval());
        js.clear();
        String[] arr = {"John","Paul","George","Ringo"};
        js.put(Arrays.asList(arr));
        js.put(Arrays.asList(arr));
        out(js.eval());

        js.clear();
        js.put("id",4);
        js.put("name","Uthred");
        js.put("alive", false);
        js.eval();

        js.clear();
        js.put(beatlesList);
        js.eval();
    }
}
