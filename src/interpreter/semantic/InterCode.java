package interpreter.semantic;

/**
 * Created by chenshaojie on 2017/9/18,15:58.
 */
public class InterCode {

    /*
     *中间代码为四元式
     */

    public static final String JMP_WITH_CONDITION = "jnt"; //有条件跳转，jmp,条件，null，目标
    public static final String JMP = "jmp";//无条件跳转,jmp,null,null,目标
    public static final String READ = "read";//read,null,null,变量
    public static final String WRITE = "write";//write,null,null,变量
    public static final String IN = "in";//in,null,null,null
    public static final String OUT = "out";//out,null,null,null
    public static final String INT = "int";//int,右值，null，左值
    public static final String REAL = "real";//real,右值，null，左值
    public static final String INT_ARR = "int[]";//int,长度，null，数组名
    public static final String REAL_ARR = "real[]";//real,长度，null，数组名
    public static final String ASSIGN = "assign";//assign,右值，null,左值
    public static final String PLUS = "+";//plus,第一个操作数，第二个操作数，结果
    public static final String MINUS = "-";
    public static final String MUL = "*";
    public static final String DIV = "/";
    public static final String LE = "<";
    public static final String EQ = "==";
    public static final String NEQ = "<>";
    public static final String ARR_ACC = "arr_acc";//数组访问，arr_acc,数组名，索引，临时变量名

    public static final InterCode inCode;
    public static final InterCode outCode;

    static {
        inCode = new InterCode();
        inCode.operation = IN;
        outCode = new InterCode();
        outCode.operation = OUT;
    }

    public enum OperandType {
        IDENTIFIER,
        INT_LITERAL,
        REAL_LITERAL,
        NULL
    }

    public String operation;//操作类型

    public OperandType firstOperandType = OperandType.NULL;//第一个操作数类型

    public OperandType secondOperandType = OperandType.NULL;//第二个操作数类型

    public String firstOperandName;//第一个操作数的标识符

    public String secondOperandName;//第二个操作数的标识符

    public String result;//结果的标识符

    public int firstOperandIntLiteral;//第一个操作数的整型字面量

    public double firstOperandRealLiteral;//第一个操作数的实数字面量

    public int secondOperandIntLiteral;//第二个操作数的整型字面量

    public double secondOperandRealLiteral;//第二个操作数的实数字面量

    public int jumpLocation;//跳转指令的语句标号

    private static StringBuilder builder = new StringBuilder();

    @Override
    public String toString() {
        builder.delete(0,builder.length());
        builder.append(operation);
        builder.append(",");
        switch (firstOperandType) {
            case INT_LITERAL:
                builder.append(firstOperandIntLiteral);
                break;
            case REAL_LITERAL:
                builder.append(firstOperandRealLiteral);
                break;
            case IDENTIFIER:
                builder.append(firstOperandName);
                break;
            case NULL:
                builder.append("null");
                break;
        }
        builder.append(",");
        switch (secondOperandType) {
            case INT_LITERAL:
                builder.append(secondOperandIntLiteral);
                break;
            case REAL_LITERAL:
                builder.append(secondOperandRealLiteral);
                break;
            case IDENTIFIER:
                builder.append(secondOperandName);
                break;
            case NULL:
                builder.append("null");
                break;
        }
        builder.append(",");
        if(operation == InterCode.JMP||operation == InterCode.JMP_WITH_CONDITION) {
            builder.append(jumpLocation);
        }
        else {
            builder.append(result);
        }

        return builder.toString();

    }
}
