package interpreter.semantic;

import interpreter.grammatical.GrammaticalParser;
import interpreter.grammatical.TreeNode;

import java.util.*;

/**
 * Created by chenshaojie on 2017/9/18,16:24.
 * 中间代码生成器
 */
public class Generator {

    private List<InterCode> codes = new ArrayList<>();//四元式列表

    private GrammaticalParser parser;

    private Stack<Integer> backFill= new Stack<Integer>();//回填序号列表
    private int tempSerialNum = 0;//临时变量的序号

    public Generator(GrammaticalParser parser) {
        this.parser = parser;
    }

    public void startGenerate() {
        generate(parser.getTreeNodes());
    }

    public List<InterCode> getCodes() {
        return codes;
    }

    private void generate(List<TreeNode> nodes) {
        for(TreeNode node:nodes) {
            switch (node.getType()) {
                case INT_DECLARATION:
                case REAL_DECLARATION:
                    generateDeclaration(node);
                    break;
                case INT_ARRAY_DECLARATION:
                case REAL_ARRAY_DECLARATION:
                    generateArrayDeclaration(node);
                    break;
                case ASSIGN:
                    generateAssign(node);
                    break;
                case IF:
                    generateIf(node);
                    break;
                case WHILE:
                    generateWhile(node);
                    break;
                case READ:
                case WRITE:
                    generateReadOrWrite(node);
                    break;
                case STATEMENT_BLOCK:
                    generateStatementBlock(node);
                    break;
                default:
                    throw new RuntimeException("unexpected statement!");
            }
        }
    }

    private String generateArithmetic(TreeNode node) {//返回算术表达式结果的变量名
        List<TreeNode> postTraversalResult = new ArrayList<>();
        postTraversal(node,postTraversalResult);
        Stack<TreeNode> stack = new Stack<>();
        for (TreeNode every: postTraversalResult) {
            switch (every.getType()) {
                case INT_LITERAL:
                case REAL_LITERAL:
                case IDENTIFIER:
                    stack.push(every);
                    break;
                case PLUS:
                    arithmeticOperationToCode(stack, TreeNode.TreeNodeType.PLUS);
                    break;
                case MINUS:
                    arithmeticOperationToCode(stack, TreeNode.TreeNodeType.MINUS);
                    break;
                case MULTIPLY:
                    arithmeticOperationToCode(stack, TreeNode.TreeNodeType.MULTIPLY);
                    break;
                case DIVIDE:
                    arithmeticOperationToCode(stack, TreeNode.TreeNodeType.DIVIDE);
                    break;
                case ARRAY_ACCESS:
                    generateArrayAccess(stack);
                    break;

            }
        }

        return stack.peek().getSymbolName();
    }

    private void generateStatementBlock(TreeNode node) {
        codes.add(InterCode.inCode);
        generate(node.getStatements());
        codes.add(InterCode.outCode);
    }

    private String generateRelational(TreeNode node) {
        InterCode code = new InterCode();
        switch (node.getType()) {
            case LESS:
                code.operation = InterCode.LE;
                break;
            case EQUAL:
                code.operation = InterCode.EQ;
                break;
            case NOT_EQUAL:
                code.operation = InterCode.NEQ;
                break;
        }
        handleOperandLeft(code,node.left);
        handleOperandRight(code,node.right);
        code.result = getNextTempName();
        codes.add(code);
        return code.result;
    }

    private void generateDeclaration(TreeNode node) {
        InterCode code = new InterCode();
        if(node.right!=null) {
            handleOperandLeft(code,node.right);
        }
        if(node.getType() == TreeNode.TreeNodeType.INT_DECLARATION) {
            code.operation = InterCode.INT;
        }
        else if(node.getType() == TreeNode.TreeNodeType.REAL_DECLARATION) {
            code.operation = InterCode.REAL;
        }
        code.result = node.left.getSymbolName();

        codes.add(code);

    }

    private void generateArrayDeclaration(TreeNode node) {
        InterCode code = new InterCode();
        if(node.getType() == TreeNode.TreeNodeType.INT_ARRAY_DECLARATION) {
            code.operation = InterCode.INT_ARR;
        }
        else if(node.getType() == TreeNode.TreeNodeType.REAL_ARRAY_DECLARATION) {
            code.operation = InterCode.REAL_ARR;
        }
        switch (node.right.getType()) {
            case INT_LITERAL:
                code.firstOperandType = InterCode.OperandType.INT_LITERAL;
                code.firstOperandIntLiteral = node.right.getIntValue();
                break;
            case IDENTIFIER:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = node.right.getSymbolName();
                break;
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = generateArithmetic(node.right);
                break;
            case ARRAY_ACCESS:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = generateArrayAccess(node.right);
        }
        code.result = node.left.getSymbolName();

        codes.add(code);
    }

    private void generateAssign(TreeNode node) {
        InterCode code = new InterCode();
        if(node.left.getType() == TreeNode.TreeNodeType.ARRAY_ACCESS) {
            Stack<TreeNode> stack = new Stack<>();
            stack.push(node.left.left);
            stack.push(node.left.right);
            code.result = generateArrayAccess(stack);
        }
        else {
            code.result = node.left.getSymbolName();
        }
        code.operation = InterCode.ASSIGN;
        handleOperandLeft(code,node.right);
        codes.add(code);
    }

    private void generateIf(TreeNode node) {
        Stack<Integer> innerBackFills = new Stack<>();//if语句块内部的待回填列表
        generateSelect(node,innerBackFills);
        if(node.getStatements().size()!=0) {
            for (TreeNode every: node.getStatements()) {
                generateSelect(every,innerBackFills);
            }
        }
        if(node.right!=null) {
            codes.add(InterCode.inCode);
            generate(node.right.getStatements());
            InterCode code1 = new InterCode();
            codes.add(InterCode.outCode);
            code1.operation = InterCode.JMP;
            codes.add(code1);
            innerBackFills.push(codes.size()-1);
        }
        int jumpLocation = codes.size();
        while (!innerBackFills.empty()) {
            int backFill = innerBackFills.pop();
            codes.get(backFill).jumpLocation = jumpLocation;
        }
    }

    private void generateWhile(TreeNode node) {
        String condition = generateRelational(node.getCondition());
        backFill.push(codes.size()-1);//指令需要回填，其索引入栈
        InterCode code = new InterCode();
        code.operation = InterCode.JMP_WITH_CONDITION;
        code.firstOperandType = InterCode.OperandType.IDENTIFIER;
        code.firstOperandName = condition;
        codes.add(code);
        codes.add(InterCode.inCode);
        generate(node.left.getStatements());
        codes.add(InterCode.outCode);
        InterCode code1 = new InterCode();
        code1.operation = InterCode.JMP;
        int jumpLocation = backFill.pop();
        code1.jumpLocation = jumpLocation;
        codes.add(code1);
        codes.get(jumpLocation+1).jumpLocation = codes.size();
    }

    private void generateSelect(TreeNode node,Stack<Integer> innerBackFills) {
        boolean needBackFill = node.getCondition() != null;
        if(needBackFill) {
            String condition = generateRelational(node.getCondition());
            InterCode code = new InterCode();
            code.operation = InterCode.JMP_WITH_CONDITION;
            code.firstOperandType = InterCode.OperandType.IDENTIFIER;
            code.firstOperandName = condition;
            codes.add(code);
            backFill.push(codes.size()-1);
        }
        codes.add(InterCode.inCode);
        generate(node.left.getStatements());
        InterCode code1 = new InterCode();
        codes.add(InterCode.outCode);
        code1.operation = InterCode.JMP;
        codes.add(code1);
        innerBackFills.push(codes.size()-1);
        if(needBackFill) {
            int jumpLocation = codes.size();//跳转目标地址为下一条指令
            int backFillInstruction = backFill.pop();//jump指令的索引
            codes.get(backFillInstruction).jumpLocation = jumpLocation;//回填目标地址
        }
    }

    private void generateReadOrWrite(TreeNode node) {
        InterCode code = new InterCode();
        switch (node.getType()) {
            case READ:
                code.operation = InterCode.READ;
            case WRITE:
                code.operation = InterCode.WRITE;
        }
        code.result = node.left.getSymbolName();

        codes.add(code);

    }

    private String getNextTempName() {//获取下一个临时变量名
        tempSerialNum++;
        return "temp"+tempSerialNum;
    }

    private void postTraversal(TreeNode arithmeticExpression,List<TreeNode> result) {
        if(arithmeticExpression == null) {
            return;
        }
        postTraversal(arithmeticExpression.left,result);
        postTraversal(arithmeticExpression.right,result);
        result.add(arithmeticExpression);
    }

    private void arithmeticOperationToCode(Stack<TreeNode> stack, TreeNode.TreeNodeType type) {
        InterCode code = new InterCode();
        switch (type) {
            case PLUS:
                code.operation = InterCode.PLUS;
                break;
            case MINUS:
                code.operation = InterCode.MINUS;
                break;
            case MULTIPLY:
                code.operation = InterCode.MUL;
                break;
            case DIVIDE:
                code.operation = InterCode.DIV;
                break;
        }
        TreeNode operand1 = stack.pop();
        TreeNode operand2 = stack.pop();
        String tempName = getNextTempName();
        switch (operand1.getType()) {
            case INT_LITERAL:
                code.firstOperandType = InterCode.OperandType.INT_LITERAL;
                code.firstOperandIntLiteral = operand1.getIntValue();
                break;
            case REAL_LITERAL:
                code.firstOperandType = InterCode.OperandType.REAL_LITERAL;
                code.firstOperandRealLiteral = operand1.getRealValue();
                break;
            case IDENTIFIER:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = operand1.getSymbolName();
                break;
        }
        switch (operand2.getType()) {
            case INT_LITERAL:
                code.secondOperandType = InterCode.OperandType.INT_LITERAL;
                code.secondOperandIntLiteral = operand2.getIntValue();
                if(code.operation.equals(InterCode.DIV)&&code.firstOperandIntLiteral == 0) {
                    throw new RuntimeException("can't divide zero!");
                }
                break;
            case REAL_LITERAL:
                code.secondOperandType = InterCode.OperandType.REAL_LITERAL;
                code.secondOperandRealLiteral = operand2.getRealValue();
                if(code.operation.equals(InterCode.DIV)&&code.firstOperandRealLiteral == 0) {
                    throw new RuntimeException("can't divide zero!");
                }
                break;
            case IDENTIFIER:
                code.secondOperandType = InterCode.OperandType.IDENTIFIER;
                code.secondOperandName = operand2.getSymbolName();
                break;
        }
        code.result = tempName;
        TreeNode temp = new TreeNode();
        temp.setType(TreeNode.TreeNodeType.IDENTIFIER);
        temp.setSymbolName(tempName);
        stack.push(temp);
        codes.add(code);
    }

    private String generateArrayAccess(Stack<TreeNode> stack) {
        InterCode code = new InterCode();
        code.operation = InterCode.ARR_ACC;
        TreeNode operand1 = stack.pop();//索引
        TreeNode operand2 = stack.pop();//数组名

        handleOperandRight(code,operand1);
        if(operand2.getType() == TreeNode.TreeNodeType.IDENTIFIER) {
            code.firstOperandType = InterCode.OperandType.IDENTIFIER;
            code.firstOperandName = operand2.getSymbolName();
        }

        code.result = getNextTempName();
        codes.add(code);

        return code.result;
    }


    private void handleOperandLeft (InterCode code, TreeNode operand) {
        switch (operand.getType()) {
            case INT_LITERAL:
                code.firstOperandType = InterCode.OperandType.INT_LITERAL;
                code.firstOperandIntLiteral = operand.getIntValue();
                break;
            case REAL_LITERAL:
                code.firstOperandType = InterCode.OperandType.REAL_LITERAL;
                code.firstOperandRealLiteral = operand.getRealValue();
                break;
            case IDENTIFIER:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = operand.getSymbolName();
                break;
            case ARRAY_ACCESS:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = generateArrayAccess(operand);
                break;
            default:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = generateArithmetic(operand);
        }
    }

    private void handleOperandRight(InterCode code, TreeNode operand) {
        switch (operand.getType()) {
            case INT_LITERAL:
                code.secondOperandType = InterCode.OperandType.INT_LITERAL;
                code.secondOperandIntLiteral = operand.getIntValue();
                break;
            case REAL_LITERAL:
                code.secondOperandType = InterCode.OperandType.REAL_LITERAL;
                code.secondOperandRealLiteral = operand.getRealValue();
                break;
            case IDENTIFIER:
                code.secondOperandType = InterCode.OperandType.IDENTIFIER;
                code.secondOperandName = operand.getSymbolName();
                break;
            case ARRAY_ACCESS:
                code.firstOperandType = InterCode.OperandType.IDENTIFIER;
                code.firstOperandName = generateArrayAccess(operand);
                break;
            default:
                code.secondOperandType = InterCode.OperandType.IDENTIFIER;
                code.secondOperandName = generateArithmetic(operand);
        }
    }

    private String generateArrayAccess(TreeNode node) {
        Stack<TreeNode> stack = new Stack<>();
        stack.push(node.left);
        stack.push(node.right);
        return generateArrayAccess(stack);
    }
}
