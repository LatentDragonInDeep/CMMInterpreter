package interpreter.execute;

import interpreter.semantic.InterCode;
import interpreter.semantic.Symbol;
import interpreter.semantic.SymbolTable;

import java.util.*;

/**
 * Created by chenshaojie on 2017/9/21,16:56.
 */
public class Interpreter {

    private int instructionIndex = 0;

    private List<InterCode> codes;

    private SymbolTable symbolTable = new SymbolTable();

    private Scanner scanner = new Scanner(System.in);

    private int blockLevel = 0;//代码块层次（在几个大括号里面）

    private boolean isFirstOperandInt;

    private boolean isSecondOperandInt;

    private Symbol condition;

    private Map<Integer,List<String>> tempVars = new HashMap<>();//保存每一层代码块的临时变量列表
    {
        List<String> level0 = new ArrayList<>();
        tempVars.put(0,level0);
    }

    public Interpreter(List<InterCode> codes) {
        this.codes = codes;
    }

    public void run() {
        while (instructionIndex < codes.size()) {
            InterCode code = codes.get(instructionIndex);
            switch (code.operation) {
                case InterCode.JMP_WITH_CONDITION:
                    jumpWithCondition(code);
                    break;
                case InterCode.JMP:
                    jump(code);
                    break;
                case InterCode.READ:
                    read(code);
                    break;
                case InterCode.WRITE:
                    write(code);
                    break;
                case InterCode.IN:
                    in();
                    break;
                case InterCode.OUT:
                    out();
                    break;
                case InterCode.INT:
                case InterCode.REAL:
                    declaration(code);
                    break;
                case InterCode.INT_ARR:
                case InterCode.REAL_ARR:
                    array(code);
                    break;
                case InterCode.ARR_ACC:
                    arrayAccess(code);
                    break;
                case InterCode.PLUS:
                case InterCode.MINUS:
                case InterCode.MUL:
                case InterCode.DIV:
                    arithmeticOperation(code);
                    break;
                case InterCode.LE:
                case InterCode.EQ:
                case InterCode.NEQ:
                    relationOperation(code);
                    break;
                case InterCode.ASSIGN:
                    assign(code);
                    break;
                default:
                    throw new RuntimeException("unexpected code!");
            }
        }
    }

    private void jump(InterCode code) {
        instructionIndex = code.jumpLocation;
    }

    private void jumpWithCondition(InterCode code) {
        if(condition.getType() == Symbol.ValueType.FALSE) {
            instructionIndex = code.jumpLocation;
        }
        else {
            nextInstruction();
        }
        condition = null;
    }

    private void read(InterCode code) {
        Symbol symbol = getSymbol(code.result);
        if(symbol.getType() == Symbol.ValueType.INT) {
            symbol.setIntValue(scanner.nextInt());
        }
        else if(symbol.getType() == Symbol.ValueType.REAL){
            symbol.setRealValue(scanner.nextDouble());
        }
        nextInstruction();
    }

    private void write(InterCode code) {
        Symbol symbol = getSymbol(code.result);
        if(symbol.getType() == Symbol.ValueType.INT) {
            System.out.println(symbol.getIntValue());
        }
        else if(symbol.getType() == Symbol.ValueType.REAL) {
            System.out.println(symbol.getRealValue());
        }
        nextInstruction();
    }

    private void in() {
        blockLevel++;
        List<String> vars = new ArrayList<>();
        tempVars.put(blockLevel,vars);
        nextInstruction();
    }

    private void out() {
        symbolTable.deleteSymbols(tempVars.get(blockLevel));
        tempVars.remove(blockLevel);
        blockLevel--;
        nextInstruction();
    }

    private void arrayAccess(InterCode code) {
        Symbol symbol = new Symbol(code.result);
        int index = 0;
        switch (code.secondOperandType) {
            case IDENTIFIER:
                index = symbolTable.getSymbol(code.secondOperandName).getIntValue();
                break;
            case INT_LITERAL:
                index = code.secondOperandIntLiteral;
                break;
        }
        Symbol array = symbolTable.getSymbol(code.firstOperandName);
        symbol.setArrayName(array.getName());
        symbol.setIndex(index);
        if(array.getType() == Symbol.ValueType.INT_ARRAY) {
            symbol.setType(Symbol.ValueType.INT_ARRAY_ELEMENT);
            symbol.setIntValue(array.getIntArray()[index]);
        }
        else if(array.getType() == Symbol.ValueType.REAL_ARRAY) {
            symbol.setType(Symbol.ValueType.REAL_ARRAY_ELEMENT);
            symbol.setRealValue(array.getRealArray()[index]);
        }
        addTempSymbol(symbol);
        nextInstruction();
    }

    private void arithmeticOperation (InterCode code) {
        double operand1 = getFirstOperand(code);
        double operand2 = getSecondOperand(code);
        Symbol symbol = new Symbol(code.result);
        switch (code.operation) {
            case InterCode.PLUS:
                if (isFirstOperandInt && isSecondOperandInt) {
                    symbol.setIntValue((int) operand1 + (int) operand2);
                    symbol.setType(Symbol.ValueType.INT);
                } else {
                    symbol.setRealValue(operand1 + operand2);
                    symbol.setType(Symbol.ValueType.REAL);
                }
            break;
            case InterCode.MINUS:
                if (isFirstOperandInt && isSecondOperandInt) {
                    symbol.setIntValue((int) operand1 - (int) operand2);
                    symbol.setType(Symbol.ValueType.INT);
                } else {
                    symbol.setRealValue(operand1 - operand2);
                    symbol.setType(Symbol.ValueType.REAL);
                }
                break;
            case InterCode.MUL:
                if (isFirstOperandInt && isSecondOperandInt) {
                    symbol.setIntValue((int) operand1 * (int) operand2);
                    symbol.setType(Symbol.ValueType.INT);
                } else {
                    symbol.setRealValue(operand1 * operand2);
                    symbol.setType(Symbol.ValueType.REAL);
                }
                break;
            case InterCode.DIV:
                if(operand2 == 0.0) {
                    throw new RuntimeException("can not divide zero!");
                }
                if (isFirstOperandInt && isSecondOperandInt) {
                    symbol.setIntValue((int) operand1 / (int) operand2);
                    symbol.setType(Symbol.ValueType.INT);
                } else {
                    symbol.setRealValue(operand1 / operand2);
                    symbol.setType(Symbol.ValueType.REAL);
                }
                break;
        }
        addTempSymbol(symbol);
        nextInstruction();
    }
    private void relationOperation(InterCode code) {
        double operand1 = getFirstOperand(code);
        double operand2 = getSecondOperand(code);
        Symbol symbol = new Symbol(code.result);
        switch (code.operation) {
            case InterCode.LE:
                if(operand1<operand2) {
                    symbol.setType(Symbol.ValueType.TRUE);
                }else {
                    symbol.setType(Symbol.ValueType.FALSE);
                }
                break;
            case InterCode.EQ:
                if(operand1 == operand2) {
                    symbol.setType(Symbol.ValueType.TRUE);
                }else {
                    symbol.setType(Symbol.ValueType.FALSE);
                }
                break;
            case InterCode.NEQ:
                if(operand1!=operand2) {
                    symbol.setType(Symbol.ValueType.TRUE);
                }else {
                    symbol.setType(Symbol.ValueType.FALSE);
                }
        }
        condition = symbol;
        nextInstruction();
    }
    private void assign(InterCode code) {
        Symbol left = symbolTable.getSymbol(code.result);
        double right = getFirstOperand(code);
        Symbol array;
        if(left.getType() == Symbol.ValueType.INT_ARRAY_ELEMENT) {
            array = symbolTable.getSymbol(left.getArrayName());
            array.getIntArray()[left.getIndex()] = (int)right;
            left.setIntValue((int)right);
        }
        else if(left.getType() == Symbol.ValueType.REAL_ARRAY_ELEMENT) {
            array = symbolTable.getSymbol(left.getArrayName());
            array.getRealArray()[left.getIndex()] = right;
            left.setRealValue(right);
        }
        else {
            switch (left.getType()) {
                case INT:
                    left.setIntValue((int) right);
                    break;
                case REAL:
                    left.setRealValue(right);
                    break;
            }
        }
        nextInstruction();
    }

    private void declaration(InterCode code) {
        Symbol symbol = new Symbol(code.result);
        double right = getFirstOperand(code);
        switch (code.operation) {
            case InterCode.INT:
                symbol.setType(Symbol.ValueType.INT);
                if (code.firstOperandType != InterCode.OperandType.NULL) {
                    symbol.setIntValue((int) right);
                }
                break;
            case InterCode.REAL:
                symbol.setType(Symbol.ValueType.REAL);
                if (code.firstOperandType != InterCode.OperandType.NULL) {
                    symbol.setRealValue(right);
                }
                break;
        }
        addTempSymbol(symbol);
        nextInstruction();
    }

    private void array(InterCode code) {
        Symbol symbol = new Symbol(code.result);
        double length = getFirstOperand(code);
        switch (code.operation) {
            case InterCode.INT_ARR:
                symbol.setIntArray(new int[(int) length]);
                symbol.setType(Symbol.ValueType.INT_ARRAY);
                break;
            case InterCode.REAL_ARR:
                symbol.setRealArray(new double[(int)length]);
                symbol.setType(Symbol.ValueType.REAL_ARRAY);
                break;
        }
        addTempSymbol(symbol);
        nextInstruction();
    }

    private Symbol getSymbol(String name) {
        return symbolTable.getSymbol(name);
    }

    private void addTempSymbol(Symbol symbol) {
        symbolTable.addSymbol(symbol);
        tempVars.get(blockLevel).add(symbol.getName());
    }

    private void nextInstruction() {
        instructionIndex++;
    }

    private double getFirstOperand(InterCode code) {
        if(code.firstOperandType == InterCode.OperandType.INT_LITERAL) {
            isFirstOperandInt = true;
            return (double)code.firstOperandIntLiteral;
        }
        else if(code.firstOperandType == InterCode.OperandType.REAL_LITERAL) {
            return code.firstOperandRealLiteral;
        }
        else {
            Symbol symbol = symbolTable.getSymbol(code.firstOperandName);
            if (symbol.getType() == Symbol.ValueType.INT||symbol.getType() == Symbol.ValueType.INT_ARRAY_ELEMENT) {
                isFirstOperandInt = true;
                return (double) symbol.getIntValue();
            } else{
                return symbol.getRealValue();
            }
        }
    }

    private double getSecondOperand(InterCode code) {
        if(code.secondOperandType == InterCode.OperandType.INT_LITERAL) {
            isSecondOperandInt = true;
            return (double)code.secondOperandIntLiteral;
        }
        else if(code.secondOperandType == InterCode.OperandType.REAL_LITERAL) {
            return code.firstOperandRealLiteral;
        }
        else {
            Symbol symbol = symbolTable.getSymbol(code.secondOperandName);
            if(symbol.getType() == Symbol.ValueType.INT||symbol.getType() == Symbol.ValueType.REAL_ARRAY_ELEMENT) {
                isSecondOperandInt = true;
                return (double)symbol.getIntValue();
            }
            else {
                return symbol.getRealValue();
            }
        }
    }
}
