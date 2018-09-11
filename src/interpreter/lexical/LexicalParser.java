package interpreter.lexical;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenshaojie on 2017/9/16,8:51.
 */
public class LexicalParser {
    private BufferedReader reader;
    private StringBuilder builder = new StringBuilder();
    private char current;
    private static final Map<Character,Integer> directRecognized = new HashMap<Character,Integer>();

    private static final HashMap<String,Integer> reserveWords = new HashMap<>();

    private static Token.TokenType[] values = Token.TokenType.values();

    private String path;

    private String sourceCode;

    private int pointer = 0;

    public LexicalParser (String path) {
        this.path = path;
    }

    public LexicalParser() {}

    static {
        directRecognized.put('+',0);
        directRecognized.put('-',1);
        directRecognized.put('*',2);
        directRecognized.put(';',15);
        directRecognized.put('(',16);
        directRecognized.put(')',17);
        directRecognized.put('{',18);
        directRecognized.put('}',19);
        directRecognized.put('[',20);
        directRecognized.put(']',21);

        reserveWords.put("if",8);
        reserveWords.put("else",9);
        reserveWords.put("while",10);
        reserveWords.put("read",11);
        reserveWords.put("write",12);
        reserveWords.put("int",13);
        reserveWords.put("real",14);
    }

    public void getSourceCode() {
        File file = new File(path);
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            while (reader.ready()) {
                builder.append(reader.readLine());
                builder.append('\n');
            }
            sourceCode = builder.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Token getNextToken() {
        Token token = new Token();
        token.setType(Token.TokenType.NULL);

                if(current == '\u0003') {
                    return token;
                }
                readCharSkip();
                if(directRecognized.containsKey(current)) {//先判断出可以直接识别的token
                    token.setType(values[directRecognized.get(current)]);
                    return token;
                }
                else if(current == '/') {//判断除号还是多行注释还是单行注释
                    readCharSkip();
                    if(current == '*') {
                        while (true) {
                            readCharSkip();
                            if(current =='*') {
                                readCharSkip();
                                if(current == '/') {
                                    token.setType(Token.TokenType.MULTIPLE_LINE_COMMENT);
                                    break;
                                }
                            }
                        }
                    }
                    else if(current == '/') {
                        readLineEnd();
                        token.setType(Token.TokenType.SINGLE_LINE_COMMENT);
                    }
                    else {
                        token.setType(Token.TokenType.DIVIDE);
                    }
                }
                else if(current == '=') {//判断赋值还是相等
                    readChar();
                    if(current == '=') {
                        token.setType(Token.TokenType.EQUAL);
                    }
                    else {
                        token.setType(Token.TokenType.ASSIGN);
                        pointer--;
                    }
                }
                else if(current == '<') {//判断小于还是不等于
                    readChar();
                    if(current =='>') {
                        token.setType(Token.TokenType.NOT_EQUAL);
                    }
                    else {
                        token.setType(Token.TokenType.LESS);
                        pointer--;
                    }
                }
                else {
                    if(current>='0'&&current<='9') {//说明接下来是一个数字字面量
                        boolean isReal = false;
                        while (true) {
                            if((current>='0'&&current<='9')||current=='.') {
                                builder.append(current);
                                if (current == '.') {
                                    isReal = true;
                                }
                                readChar();
                            }
                            else {
                                pointer--;
                                break;
                            }
                        }
                        String value = builder.toString();
                        builder.delete(0,builder.length());
                        if(isReal) {
                            token.setType(Token.TokenType.REAL_LITERAL);
                            token.setRealValue(Double.parseDouble(value));
                        }
                        else {
                            token.setType(Token.TokenType.INT_LITERAL);
                            token.setIntValue(Integer.parseInt(value));
                        }
                    }
                    else if((current>='A'&&current<='Z')||(current>='a'&&current<='z')){//说明接下来是一个标识符或者关键字
                        while (true) {
                            if((current>='A'&&current<='Z')||(current>='a'&&current<='z')||(current>='0'&&current<='9')||current == '_') {
                                builder.append(current);
                                readChar();
                            }
                            else {
                                pointer--;
                                break;
                            }
                        }
                        String value = builder.toString();
                        builder.delete(0,builder.length());
                        if(reserveWords.containsKey(value)) {
                            token.setType(values[reserveWords.get(value)]);
                        }
                        else {
                            token.setType(Token.TokenType.IDENTIFIER);
                            token.setStringValue(value);
                        }
                    }
                }


        return token;
    }

    private void readCharSkip () {
        do {
            if (pointer < sourceCode.length()) {
                current = sourceCode.charAt(pointer);
                pointer++;
            } else {
                current = '\u0003';
                break;
            }
        }while (current == '\n'||current == '\r'||current == '\t'||current ==' ');
    }

    private void readLineEnd() {
        while (sourceCode.charAt(pointer)!='\n') {
            pointer++;
        }
    }

    private void readChar() {
        if (pointer < sourceCode.length()) {
            current = sourceCode.charAt(pointer);
            pointer++;
        } else {
            current = '\u0003';
        }
    }
}
