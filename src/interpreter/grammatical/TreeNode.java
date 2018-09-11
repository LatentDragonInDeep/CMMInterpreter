package interpreter.grammatical;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenshaojie on 2017/9/17,10:20.
 */
public class TreeNode {

    public enum TreeNodeType {


        INT_DECLARATION,//整型变量声明,left存储变量名，right(如果有的话)存储赋值表达式
        REAL_DECLARATION,//实数变量声明,同上

        INT_ARRAY_DECLARATION,//整型数组声明,left存储数组标识符，right存储数组大小
        REAL_ARRAY_DECLARATION,//实数数组声明，同上

        ARRAY_ACCESS,//数组访问，left存储数组标识符，right存储索引

        ASSIGN,//变量赋值,left变量名,right赋值表达式
        READ,//read语句,left变量名
        WRITE,//write语句

        PLUS,//加法运算,left第一个操作数，right第二个操作数
        MINUS,//减法运算
        MULTIPLY,//乘法运算
        DIVIDE,//除法运算

        LESS,//小于关系,left第一个操作数，right第二个操作数
        EQUAL,//等于关系
        NOT_EQUAL,//不等关系

        /*
         *if条件为真的语句块存储在left
         * 如果有else语句块，存储在right
         * 所有的else if存储在statementBlock里面
         */
        IF,
        ELSE,//else语句
        ELSE_IF,
        /*
         *while语句块存储在left
         */
        WHILE,//while语句

        STATEMENT_BLOCK,//语句块，块内容存储在statementBlock域

        INT_LITERAL, //整型字面量
        REAL_LITERAL,//实数字面量
        IDENTIFIER,//标识符

        NULL,



    }

    public TreeNode left;
    public TreeNode right;

    private String symbolName;//标识符的名字
    private int intValue;//整型值
    private double realValue;//实数值

    private LinkedList<TreeNode> statementBlock = new LinkedList<>();//选择结构或者循环结构的语句块

    private TreeNode condition;//while或者if的条件表达式

    private TreeNodeType type;//treenode的类型

    public String getSymbolName () {
        return symbolName;
    }

    public void setSymbolName (String symbolName) {
        this.symbolName = symbolName;
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

    public TreeNode getCondition () {
        return condition;
    }

    public void setCondition (TreeNode condition) {
        this.condition = condition;
    }

    public TreeNodeType getType () {
        return type;
    }

    public void setType (TreeNodeType type) {
        this.type = type;
    }

    public void addStatement(TreeNode node) {
        statementBlock.add(node);
    }

    public List<TreeNode> getStatements() {
        return statementBlock;
    }
}
