package interpreter.semantic;

/**
 * Created by chenshaojie on 2017/9/18,16:02.
 */
public class Symbol {

    public Symbol (String name) {
        this.name = name;
    }

    public enum ValueType {
        INT,
        REAL,
        INT_ARRAY,
        REAL_ARRAY,
        TRUE,
        FALSE,
        INT_ARRAY_ELEMENT,
        REAL_ARRAY_ELEMENT
    }

    private ValueType type;

    private int intValue;
    private double realValue;

    private int[] intArray;
    private double[] realArray;

    private String arrayName;//原始数组的名字

    private int index;//原始数组的索引

    private final String name;//符号的名字

    private int level;//符号的层次

    public Symbol next;//比该符号低一个层次的符号

    public int getIntValue () {
        return intValue;
    }

    public void setIntValue (int intValue) {
        this.intValue = intValue;
    }

    public double getRealValue () {
        return realValue;
    }

    public void setRealValue (double realValue) {
        this.realValue = realValue;
    }

    public int[] getIntArray () {
        return intArray;
    }

    public void setIntArray (int[] intArray) {
        this.intArray = intArray;
    }

    public double[] getRealArray () {
        return realArray;
    }

    public void setRealArray (double[] realArray) {
        this.realArray = realArray;
    }

    public ValueType getType () {
        return type;
    }

    public void setType (ValueType type) {
        this.type = type;
    }

    public String getName () {
        return name;
    }

    public int getLevel () {
        return level;
    }

    public void setLevel (int level) {
        this.level = level;
    }

    public String getArrayName () {
        return arrayName;
    }

    public void setArrayName (String arrayName) {
        this.arrayName = arrayName;
    }

    public int getIndex () {
        return index;
    }

    public void setIndex (int index) {
        this.index = index;
    }
}
