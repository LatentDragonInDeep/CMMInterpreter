package interpreter.grammatical;

import interpreter.lexical.LexicalParser;
import interpreter.lexical.Token;

import java.util.*;

/**
 * Created by chenshaojie on 2017/9/17,10:19.
 */

//采用递归下降语法分析方法
    //生成抽象语法树
    /*
    * 完整cmm文法
    program -> stmt-sequence
    stmt-sequence -> statement ; stmt-sequence | statement | ε
    statement -> if-stmt | while-stmt | assign-stmt | read-stmt | write-stmt | declare-stmt
    stmt-block -> statement | { stmt-sequence }
    if-stmt -> if ( exp ) then stmt-block | if ( exp ) then stmt-block else stmt-block
    while-stmt -> while ( exp ) stmt-block
    assign-stmt -> variable = exp ;
    read-stmt -> read variable ;
    write-stmt -> write exp ;
    declare-stmt -> (int | real) ( (identifier [= exp ]) | (identifier [ exp ]) ) ;
    variable -> identifier [ [ exp ] ]
    exp -> addtive-exp logical-op addtive-exp | addtive-exp
    addtive-exp -> term add-op additive-exp | term
    term -> factor mul-op term | factor
    factor -> ( exp ) | number | variable | Add-op exp
    logical-op -> > | < | >= | <= | <> | ==
    add-op -> + | -
    mul-op -> * | /
    *
    * */
public class GrammaticalParser {

    private LexicalParser lexicalParser;//上一级的词法分析器

    private Token current;//当前token

    private List<TreeNode> treeNodes = new ArrayList<>();//存储每一棵语法树

    public GrammaticalParser(LexicalParser lexicalParser) {
        this.lexicalParser = lexicalParser;
    }

    private void getNextToken() {
        do {
            current = lexicalParser.getNextToken();
        }while (current.getType()== Token.TokenType.SINGLE_LINE_COMMENT||current.getType() == Token.TokenType.MULTIPLE_LINE_COMMENT||current == null);
    }

    public void startParse() {
        while (true) {
            TreeNode node = parseStatement(false,true);
            if(node.getType() == TreeNode.TreeNodeType.NULL) {
                break;
            }
        }
    }

    private TreeNode parseStatement(boolean isRecursive/*指示是否是递归调用*/,boolean needNext) {
         if(current == null||current.getType() == Token.TokenType.SEMICOLON||needNext) {
             getNextToken();
         }
         TreeNode node = new TreeNode();
         node.setType(TreeNode.TreeNodeType.NULL);
         if(current.getType()!= Token.TokenType.NULL) {
             switch (current.getType()) {
                 case INT:
                     node = parseDeclarationStatement(Token.TokenType.INT);
                     break;
                 case REAL:
                     node = parseDeclarationStatement(Token.TokenType.REAL);
                     break;
                 case IDENTIFIER:
                     node = parseAssignStatement();
                     break;
                 case READ:
                     node = parseReadOrWriteStatement(Token.TokenType.READ);
                     break;
                 case WRITE:
                     node = parseReadOrWriteStatement(Token.TokenType.WRITE);
                     break;
                 case IF:
                     node = parseIfStatement();
                     break;
                 case WHILE:
                     node = parseWhileStatement();
                     break;
                 case L_ANGLE_BRACKET:
                     node = parseStatementBlock(false);
                     break;
                 default:
                     throw new RuntimeException("unexpected token!");
             }
         }
         if(!isRecursive&&node.getType()!= TreeNode.TreeNodeType.NULL) {
             treeNodes.add(node);
         }

         return node;
    }

    private TreeNode parseStatementBlock(boolean needNext) {
        TreeNode node = new TreeNode();
        node.setType(TreeNode.TreeNodeType.STATEMENT_BLOCK);
        if(needNext) {
            matchTokenNext(Token.TokenType.L_ANGLE_BRACKET);
        }
        else {
            matchToken(Token.TokenType.L_ANGLE_BRACKET);
        }
        while (!checkTokenNext(Token.TokenType.R_ANGLE_BRACKET)) {
            node.addStatement(parseStatement(true,false));
        }

        return node;

    }

    private TreeNode parseDeclarationStatement(Token.TokenType type) {
        TreeNode node = new TreeNode();
        boolean isArray = false;
        matchTokenNext(Token.TokenType.IDENTIFIER);
        TreeNode left = new TreeNode();
        left.setType(TreeNode.TreeNodeType.IDENTIFIER);
        left.setSymbolName(current.getStringValue());
        node.left = left;
        getNextToken();
        if(checkToken(Token.TokenType.ASSIGN)) {
            node.right = parseArithmeticExpression();
            matchToken(Token.TokenType.SEMICOLON);
        }
        else if(checkToken(Token.TokenType.L_SQUARE_BRACKET)) {
            node.right = parseArithmeticExpression();
            matchToken(Token.TokenType.R_SQUARE_BRACKET);
            isArray = true;
            matchTokenNext(Token.TokenType.SEMICOLON);
        }
        switch (type) {
            case INT:
                if(isArray) {
                    node.setType(TreeNode.TreeNodeType.INT_ARRAY_DECLARATION);
                }
                else {
                    node.setType(TreeNode.TreeNodeType.INT_DECLARATION);
                }
                break;
            case REAL:
                if(isArray) {
                    node.setType(TreeNode.TreeNodeType.REAL_ARRAY_DECLARATION);
                }
                else {
                    node.setType(TreeNode.TreeNodeType.REAL_DECLARATION);
                }
        }

        return node;
    }

    private TreeNode parseAssignStatement() {
        TreeNode node = new TreeNode();
        node.setType(TreeNode.TreeNodeType.ASSIGN);
        TreeNode left = new TreeNode();
        List<Token> tokens = new ArrayList<>();
        left.setType(TreeNode.TreeNodeType.IDENTIFIER);
        left.setSymbolName(current.getStringValue());
        tokens.add(current);
        getNextToken();
        switch (current.getType()) {
            case ASSIGN:
                node.left = left;
                node.right = parseArithmeticExpression();
                matchToken(Token.TokenType.SEMICOLON);
                break;
            case L_SQUARE_BRACKET:
                do {
                    tokens.add(current);
                    getNextToken();
                }while (current.getType() != Token.TokenType.R_SQUARE_BRACKET);
                tokens.add(current);
                node.left = parseArrayAccess(tokens);
                matchTokenNext(Token.TokenType.ASSIGN);
                node.right = parseArithmeticExpression();
                matchToken(Token.TokenType.SEMICOLON);
        }

        return node;
    }

    private TreeNode parseReadOrWriteStatement(Token.TokenType type) {
        TreeNode node = new TreeNode();
        switch (type) {
            case READ:
                node.setType(TreeNode.TreeNodeType.READ);
            case WRITE:
                node.setType(TreeNode.TreeNodeType.WRITE);
        }
        TreeNode left = new TreeNode();
        left.setType(TreeNode.TreeNodeType.IDENTIFIER);
        matchTokenNext(Token.TokenType.IDENTIFIER);
        left.setSymbolName(current.getStringValue());
        node.left = left;
        matchTokenNext(Token.TokenType.SEMICOLON);

        return node;
    }

    private TreeNode parseIfStatement() {
        TreeNode node = new TreeNode();
        node.setType(TreeNode.TreeNodeType.IF);
        matchTokenNext(Token.TokenType.L_BRACKET);
        node.setCondition(parseRelationalExpression());
        matchToken(Token.TokenType.R_BRACKET);
        node.left = parseStatementBlock(true);
        while (true) {
            if (checkTokenNext(Token.TokenType.ELSE)) {
                if (checkTokenNext(Token.TokenType.IF)) {
                    TreeNode elseIf= new TreeNode();
                    elseIf.setType(TreeNode.TreeNodeType.ELSE_IF);
                    matchTokenNext(Token.TokenType.L_BRACKET);
                    elseIf.setCondition(parseRelationalExpression());
                    matchToken(Token.TokenType.R_BRACKET);
                    elseIf.left = parseStatementBlock(true);
                    node.addStatement(elseIf);
                }
                else {
                    node.right = parseStatementBlock(false);
                    break;
                }
            }
            else {
                break;
            }
        }

        return node;
    }

    private TreeNode parseWhileStatement() {
        TreeNode node = new TreeNode();
        node.setType(TreeNode.TreeNodeType.WHILE);
        matchTokenNext(Token.TokenType.L_BRACKET);
        node.setCondition(parseRelationalExpression());
        matchToken(Token.TokenType.R_BRACKET);
        node.left = parseStatementBlock(true);

        return node;
    }

    private TreeNode parseArithmeticExpression() {
        List<Token> tokens = getAllExpressionTokens();
        return parseArithmeticExpression(tokens);
    }

    private TreeNode parseArithmeticExpression(List<Token> tokens) {
        Stack<TreeNode> operandStack = new Stack<>();//操作数栈
        Stack<TreeNode> operatorStack = new Stack<>();//操作符栈
        Stack<Character> brackets = new Stack<>();//括号栈
        ListIterator<Token> iterator = tokens.listIterator();

        while (true){
            if(iterator.hasNext()) {
                Token token = iterator.next();
                if (checkTokenOperand(token)) {
                    if (iterator.hasNext()) {
                        if (iterator.next().getType() != Token.TokenType.L_SQUARE_BRACKET) {
                            operandStack.push(tokenToTreeNode(token));
                            iterator.previous();
                        } else {
                            int start = iterator.previousIndex();
                            int end;
                            brackets.push('[');
                            Token aim = null;
                            while (!brackets.empty()) {
                                aim = iterator.next();
                                if (aim.getType() == Token.TokenType.L_SQUARE_BRACKET) {
                                    brackets.push('[');
                                } else if (aim.getType() == Token.TokenType.R_SQUARE_BRACKET) {
                                    brackets.pop();
                                }
                            }
                            end = iterator.nextIndex();

                            operandStack.push(parseArrayAccess(tokens.subList(start-1, end)));
                        }
                    }
                    else {
                        operandStack.push(tokenToTreeNode(token));
                    }
                } else if (checkTokenArithmeticOperator(token)) {
                    if (operatorStack.empty()) {
                        operatorStack.push(tokenToTreeNode(token));
                    } else {
                        TreeNode currentOperator = tokenToTreeNode(token);
                        TreeNode previousOperator = operatorStack.peek();
                        if (priorityCompare(previousOperator, currentOperator)) {
                            Token next = iterator.next();
                            TreeNode currentOperand;
                            if (!checkTokenBracket(next)) {//如果下一个token不是左括号
                                currentOperand = tokenToTreeNode(next);
                            } else {//如果下一个token是左括号
                                //从后往前遍历找右括号
                                currentOperand = sliceExpressionInBrackets(iterator,tokens);
                            }
                            TreeNode previousOperand = operandStack.pop();
                            currentOperator.right = currentOperand;
                            currentOperator.left = previousOperand;
                            operandStack.push(currentOperator);
                        } else {

                            TreeNode operand1 = operandStack.pop();
                            TreeNode operand2 = operandStack.pop();
                            previousOperator.left = operand1;
                            previousOperator.right = operand2;
                            operandStack.push(previousOperator);
                            operatorStack.push(currentOperator);
                        }
                    }
                }
                else if(token.getType() == Token.TokenType.L_BRACKET) {
                    TreeNode operand = sliceExpressionInBrackets(iterator,tokens);
                    operandStack.push(operand);
                }
                else {
                    throw new RuntimeException("unexpected token!");
                }
            }

            else  {//如果没有需要处理的token
                if(operandStack.size()>1) {
                    TreeNode operand1 = operandStack.pop();
                    TreeNode operand2 = operandStack.pop();
                    TreeNode operator = operatorStack.pop();
                    operator.left = operand1;
                    operator.right = operand2;
                    operandStack.push(operator);
                }
                else {
                    break;
                }
            }

        }

        return operandStack.peek();
    }

    //获得表达式的所有token
    private List<Token> getAllExpressionTokens() {
        List<Token> tokens = new ArrayList<>();
        Stack<Character> s = new Stack<>();//小括号栈
        Stack<Character> s2 = new Stack<>();//中括号栈
        loop:while (true) {
            getNextToken();
            switch (current.getType()) {
                case IDENTIFIER:
                case PLUS:
                case MINUS:
                case MULTIPLY:
                case DIVIDE:
                case INT_LITERAL:
                case REAL_LITERAL:
                    tokens.add(current);
                    break;
                case L_BRACKET:
                    tokens.add(current);
                    s.push('(');
                    break;
                case R_BRACKET:
                    if(s.empty()) {//如果栈为空遇到右括号，则表达式结束
                        break loop;
                    }
                    else if(s.peek() == '(') {
                        s.pop();
                        tokens.add(current);
                    }
                    else {
                        throw new RuntimeException("unexpected token!");
                    }
                    break;
                case L_SQUARE_BRACKET:
                    tokens.add(current);
                    s2.push('[');
                    break;
                case R_SQUARE_BRACKET:
                    if(s2.empty()) {//如果栈为空遇到右括号，则表达式结束
                        break loop;
                    }
                    else if(s2.peek() == '[') {
                        s2.pop();
                        tokens.add(current);
                    }
                    else {
                        throw new RuntimeException("unexpected token!");
                    }
                    break;
                default:
                    //如果遇到上述token以外的其它token，表达式结束
                    break loop;
            }
        }

        return tokens;
    }

    private TreeNode parseRelationalExpression() {
        TreeNode node = new TreeNode();
        node.left = parseArithmeticExpression();
        if(checkTokenRelationalOperator()) {
            switch (current.getType()) {
                case LESS:
                    node.setType(TreeNode.TreeNodeType.LESS);
                    break;
                case EQUAL:
                    node.setType(TreeNode.TreeNodeType.EQUAL);
                    break;
                case NOT_EQUAL:
                    node.setType(TreeNode.TreeNodeType.NOT_EQUAL);
                    break;
            }
        }
        else {
            throw new RuntimeException("unexpected token!");
        }
        node.right = parseArithmeticExpression();

        return node;
    }



    private void matchTokenNext (Token.TokenType type) {
        getNextToken();
        if(type!=current.getType()) {
            throw new RuntimeException("unexpected token!");
        }
    }

    private void matchToken(Token.TokenType type) {
        if(type!=current.getType()) {
            throw new RuntimeException("unexpected token!");
        }
    }

    private boolean checkTokenNext (Token.TokenType type) {
        getNextToken();
        return current.getType() == type;
    }

    private boolean checkToken(Token.TokenType type) {
        return current.getType() == type;
    }


    private boolean checkTokenRelationalOperator() {
        Token.TokenType type = current.getType();
        return type == Token.TokenType.LESS||
                type == Token.TokenType.EQUAL||
                type == Token.TokenType.NOT_EQUAL;
    }

    private boolean checkTokenArithmeticOperator(Token token) {
        Token.TokenType type = token.getType();
        return type == Token.TokenType.PLUS||
                type == Token.TokenType.MINUS||
                type == Token.TokenType.MULTIPLY||
                type == Token.TokenType.DIVIDE;
    }

    private TreeNode tokenToTreeNode(Token token) {
        TreeNode node = new TreeNode();
        switch (token.getType()) {
            case IDENTIFIER:
                node.setType(TreeNode.TreeNodeType.IDENTIFIER);
                node.setSymbolName(token.getStringValue());
                break;
            case INT_LITERAL:
                node.setType(TreeNode.TreeNodeType.INT_LITERAL);
                node.setIntValue(token.getIntValue());
                break;
            case REAL_LITERAL:
                node.setType(TreeNode.TreeNodeType.REAL_LITERAL);
                node.setRealValue(token.getRealValue());
                break;
            case PLUS:
                node.setType(TreeNode.TreeNodeType.PLUS);
                break;
            case MINUS:
                node.setType(TreeNode.TreeNodeType.MINUS);
                break;
            case MULTIPLY:
                node.setType(TreeNode.TreeNodeType.MULTIPLY);
                break;
            case DIVIDE:
                node.setType(TreeNode.TreeNodeType.DIVIDE);
                break;
        }
        return node;
    }

    private boolean priorityCompare(TreeNode operator1,TreeNode operator2) {//比较运算符优先级
        return getPriority(operator1)<getPriority(operator2);
    }

    private int getPriority(TreeNode operator) {//获得运算符优先级
        if(operator.getType() == TreeNode.TreeNodeType.PLUS||operator.getType() == TreeNode.TreeNodeType.MINUS) {
            return 1;
        }
        else if(operator.getType() == TreeNode.TreeNodeType.MULTIPLY||operator.getType() == TreeNode.TreeNodeType.DIVIDE) {
            return 2;
        }
        throw new RuntimeException("unexpected operator!");
    }

    private boolean checkTokenOperand(Token token) {
        Token.TokenType type = token.getType();
        return type == Token.TokenType.IDENTIFIER
                ||type == Token.TokenType.INT_LITERAL
                ||type == Token.TokenType.REAL_LITERAL;
    }

    private boolean checkTokenBracket(Token token) {
        return token.getType() == Token.TokenType.L_BRACKET;
    }


    public List<TreeNode> getTreeNodes() {
        return treeNodes;
    }

    private TreeNode parseArrayAccess(List<Token> tokens) {
        TreeNode node = new TreeNode();
        node.setType(TreeNode.TreeNodeType.ARRAY_ACCESS);
        TreeNode left = new TreeNode();
        left.setType(TreeNode.TreeNodeType.IDENTIFIER);
        left.setSymbolName(tokens.get(0).getStringValue());
        node.left = left;
        if(tokens.size() == 4) {
            Token right = tokens.get(2);
            node.right = tokenToTreeNode(right);
        }else {
            node.right = parseArithmeticExpression(tokens.subList(2, tokens.size() - 1));
        }

        return node;
    }

    private TreeNode sliceExpressionInBrackets(ListIterator<Token> iterator,List<Token> tokens) {
        Stack<Character> brackets = new Stack<>();
        int start = iterator.previousIndex();
        int end;
        brackets.push('(');
        Token aim = null;
        while (!brackets.empty()) {
            aim = iterator.next();
            if (aim.getType() == Token.TokenType.L_BRACKET) {
                brackets.push('(');
            } else if (aim.getType() == Token.TokenType.R_BRACKET) {
                brackets.pop();
            }
        }
        end = iterator.nextIndex();
        return parseArithmeticExpression(tokens.subList(start + 1, end - 1));
    }
}
