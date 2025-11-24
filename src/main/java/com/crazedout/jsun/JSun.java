// Copyright (c) 2023 CrazedoutSoft / Fredrik Roos
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is furnished
// to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
// PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
// OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// info@crazedout.com
package com.crazedout.jsun;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class to serialize objects to Json object / string.<br>
 * User specified key/value pairs or<br>
 * declared public or protected getters on Object will be parsed to Json string.<br>
 * <pre>
 *     <code>
 *         Examples:
 *         JSun js = new JSun();
 *         js.put("id",4);
 *         js.put("name","Uthred");
 *         js.put("alive", false);
 *         String jStr = js.eval();
 *          <i>{"id":4,"name":"Uthred","alive":false}</i>
 *
 *         js.clear();
 *         List<Person> beatlesList = new ArrayList<>();
 *         beatlesList.add(new Person("John", "Guitar"));
 *         beatlesList.add(new Person("Paul", "Bass"));
 *         beatlesList.add(new Person("George", "Guitar"));
 *         beatlesList.add(new Person("Ringo", "Drums"));
 *         js.put("Beatles", beatlesList);
 *         String jsonString = js.eval();
 *         <i>{"Beatles":[{"name":"John","instrument":"Guitar"},{"name":"Paul","instrument":"Bass"},
 *            {"name":"George","instrument":"Guitar"},{"name":"Ringo","instrument":"Drums"}]}
 *         </i>
 *     </code>
 * </pre>
 * It's highly recommended to use the <a href="https://projectlombok.org/">Project Lombrok</a> and it's
 * Getter and Setter annotations för JSun objects.<br>
 *
 * @author Fredrik Roos 2023.
 * @see <a href="#">@JSunClass</a>
 */
public class JSun {

    private final List<KeyValuePair> keyPairList = new LinkedList<>();
    private List<String> excludeList;
    private final ScriptEngineManager factory = new ScriptEngineManager();
    private final ScriptEngine engine = factory.getEngineByName("JavaScript");
    private static final String ARRAY_TAG = "<JSON_ARRAY>";

    static class KeyValuePair {
        String name;
        Object value;

        KeyValuePair(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class JsonArray {
        List<Object> arrList = new LinkedList<>();

        JsonArray(Object... obj) {
            //arrList.addAll(Arrays.asList(obj));
            for(Object o:obj){
                if(!(o instanceof String)
                        && !(o instanceof JSun)
                        && !(o instanceof Float)
                        && !(o instanceof Double)
                        && !(o instanceof Integer)){
                    arrList.add(new JSun(o));
                }else{
                    arrList.add(o);
                }
            }
        }
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            for(Object o:arrList) sb.append(o.toString()).append(",");
            return sb.toString();
        }
    }

    /**
     * Creates a JSun object.
     */
    public JSun() {
    }

    /**
     * Creates a JSun with initial key/value pair.
     *
     * @param key  Json name
     * @param value Json value
     */
    public JSun(String key, Object value) {
        put(key, value);
    }

    /**
     * Creates a JSun object with initial List.
     * @param list list to be jsonifed.
     */
    public JSun(List<?> list){
        put(list);
    }

    /**
     * Creates a JSun with initial key/[list].
     *
     * @param key Json name
     * @param objs Json array
     */
    public JSun(String key, Object... objs) {
        put(key, objs);
    }

    /**
     * Creates a JSun with initial key/[list].
     *
     * @param key   Json name
     * @param values Json list
     */
    public JSun(String key, List<?> values) {
        put(key, values);
    }

    /**
     * Creates a JSun for obj.<br>
     * All declared public and protected getters (e.i getName()) will be jsonified.<br>
     * The Class of obj should be annotated with @JSunClass annotation, but any pojo will work.<br>
     * With @JSunClass getters can be excluded as @JSunClass(exclude={"id","title"}.<br>
     *
     * @param obj Object to be Json serialized.
     */
    public JSun(Object obj) {
        if(obj==null) throw new RuntimeException("Object must not be null.");
        if (obj.getClass().getAnnotation(JSunClass.class) != null) {
            excludeList = Arrays.asList(obj.getClass().getAnnotation(JSunClass.class).exclude());
        }
        this.recurseObject(obj);
    }

    /**
     * Fast track to (new JSun(list).eval());
     * @param list list to be parsed to json
     * @return String json
     * @throws javax.script.ScriptException
     */
    public static String toJSon(List<?> list) throws javax.script.ScriptException{
        return new JSun(list).eval();
    }

    /**
     * Fast track to (new JSun(object).eval());
     * @param obj to be parsed so Json
     * @return String json
     * @throws javax.script.ScriptException
     */
    public static String toJSon(Object obj) throws javax.script.ScriptException{
        return new JSun(obj).eval();
    }

    private void recurseObject(Object obj) {
        List<Method> list = findGetters(obj.getClass());
        for (Method m : list) {
            if (m.getName().startsWith("get") && m.getName().length() > 3) {
                if (excludeList != null && excludeList.contains(m.getName().substring(3).toLowerCase())) continue;
                try {
                    String name = m.getName().toLowerCase().substring(3);
                    Object value = m.invoke(obj);
                    if (value instanceof List<?>) {
                        keyPairList.add(new KeyValuePair(name, new JsonArray(((List<?>) value).toArray())));
                    } else {
                        keyPairList.add(new KeyValuePair(name, value));
                    }
                } catch (Exception ex) {
                    //throw new RuntimeException(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Evaluates the input javascript string through<br>
     * ScriptEngine JavaScript eval().
     * @throws ScriptException if scriptEngine.eval(javascriptString) fails.
     */
    public static void eval(String javascriptString) throws ScriptException{
        (new JSun()).engine.eval(javascriptString);
    }

    /**
     * Evaluates the json string of this JSun object through<br>
     * ScriptEngine JavScript eval() and returns string.
     * @return json string
     * @throws ScriptException if scriptEngine.eval("JSON.parse('" + toJsonString() +"');") fails.
     */
    public String eval() throws ScriptException {
        String js = "JSON.parse('" + toJsonString() +"');";
        engine.eval(js);
        return toJsonString();
    }

    public List<KeyValuePair> getKeyPairList(){
        return this.keyPairList;
    }

    /**
     * Clear all keys and values.
     */
    public void clear(){
        this.keyPairList.clear();
        if(excludeList!=null) this.excludeList.clear();
    }

    /**
     * Generates Json string as {"key":[obj1,obj2,obj3...]}
     *
     * @param key json key
     * @param objs json array
     */
    public void put(String key, Object... objs) {
        JsonArray arr = new JsonArray(objs);
        this.put(key, arr);
    }

    /**
     * Generates Json string as {"key":[val1,val2,val3...]}
     *
     * @param key   Json key
     * @param values Json array
     */
    public void put(String key, List<?> values) {
        JsonArray arr = new JsonArray(values.toArray());
        this.put(key, arr);
    }

    /**
     * Adds at List
     * @param list list to be jsonified.
     */
    public void put(List<?> list){
        this.put(ARRAY_TAG, list);
    }

    /**
     * Generates Json string as {"key":"value"} or {"name":value} if number.
     *
     * @param key  Json key
     * @param value Json value
     */
    public void put(String key, Object value) {
        if (checkKey(key)) {
            keyPairList.add(new KeyValuePair(key, value));
        } else {
            throw new RuntimeException("Duplicate key '" + key + "'.");
        }
    }

    /**
     * Check for duplicate key.
     *
     * @param key Json key
     * @return true/false
     */
    private boolean checkKey(String key) {
        if(key==null) throw new RuntimeException("Key may not be null.");
        for (KeyValuePair obj : keyPairList) {
            if (obj.name.equals(key) && !obj.name.equals(ARRAY_TAG)) return false;
        }
        return true;
    }

    /**
     * Gets the Jsonified string created by JSun. Same as toString();
     * @return json string
     */
    public String toJsonString(){
        return this.toString();
    }

    @Override
    public String toString() {
        if(keyPairList.size()==1 && keyPairList.get(0).name.equals("<JSON_ARRAY>")) return parse(this);
        else if(keyPairList.size()>1 && keyPairList.get(0).name.equals("<JSON_ARRAY>")) return "[" + parse(this) + "]";
        else return  "{" + parse(this) + "}";
    }

    private String parse(JSun jsun) {
        StringBuilder json = new StringBuilder();
        for (KeyValuePair np : jsun.keyPairList) {
            json.append(parseKeyValuePair(np));
        }
        if (json.toString().trim().length() > 0 && json.toString().trim().endsWith(",")) {
            json = new StringBuilder(json.substring(0, json.length() - 1));
        }
        return json.toString();
    }

    private String parseKeyValuePair(KeyValuePair np) {
        StringBuilder json = new StringBuilder();
        String name = np.name;
        Object value = np.value;
        if(!name.equals(ARRAY_TAG)) {
            json.append("\"").append(name).append("\":");
        }
        if (value instanceof JSun) {
            json.append(value);
        } else if (value instanceof JsonArray) {
            List<Object> arrList = ((JsonArray) value).arrList;
            json.append("[");
            for (Object ao : arrList) {
                if (ao instanceof KeyValuePair) parseKeyValuePair((KeyValuePair) ao);
                else if (ao instanceof String) json.append("\"").append(ao).append("\",");
                else {
                    json.append(ao).append(",");
                }
            }
            if (json.toString().trim().length() > 0 && json.toString().trim().endsWith(",")) {
                json = new StringBuilder(json.substring(0, json.length() - 1));
            }
            json.append("],");
        } else if (value instanceof String) {
            json.append("\"").append(value).append("\",");
        } else {
            json.append(value).append(",");
        }
        return json.toString();
    }

    private ArrayList<Method> findGetters(Class<?> c) {
        ArrayList<Method> list = new ArrayList<>();
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods)
            if (isGetter(method))
                list.add(method);
        return list;
    }

    private boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
                method.getParameterTypes().length == 0) {
            if (method.getName().matches("^get[A-Z].*") &&
                    !method.getReturnType().equals(void.class))
                return true;
            return method.getName().matches("^is[A-Z].*") &&
                    method.getReturnType().equals(boolean.class);
        }
        return false;
    }
}

