package interpreter.lexical;

/**
 * Created by chenshaojie on 2017/9/16,8:51.
 */
public class Token {

    public enum TokenType {

        //算术运算符
        PLUS,//0
        MINUS,//1
        MULTIPLY,//2
        DIVIDE,//3
        ASSIGN,//4

        //关系运算符
        LESS,//5
        EQUAL,//6
        NOT_EQUAL,//7

        //保留字
        IF,//8
        ELSE,//9
        WHILE,//10
        READ,//11
        WRITE,//12
        INT,//13
        REAL,//14

        //分隔符
        SEMICOLON,//15
        L_BRACKET,//16
        R_BRACKET,//17
        L_ANGLE_BRACKET,//18
        R_ANGLE_BRACKET,//19
        L_SQUARE_BRACKET,//20
        R_SQUARE_BRACKET,//21

        //字面量
        INT_LITERAL,//24
        REAL_LITERAL,//25
        IDENTIFIER,//26

        //注释
        SINGLE_LINE_COMMENT,
        MULTIPLE_LINE_COMMENT,

        NULL//空的token，说明已经到文件结尾
    }

    private TokenType type;//token类型
    private String stringValue;//字符串值
    private int intValue;//整型值
    private double realValue;//实数值

    public TokenType getType () {
        return type;
    }

    public void setType (TokenType type) {
        this.type = type;
    }

    public String getStringValue () {
        return stringValue;
    }

    public void setStringValue (String stringValue) {
        this.stringValue = stringValue;
    }

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
}
