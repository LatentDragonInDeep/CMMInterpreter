package interpreter;

import interpreter.execute.Interpreter;
import interpreter.grammatical.GrammaticalParser;
import interpreter.lexical.LexicalParser;
import interpreter.semantic.Generator;
import interpreter.semantic.InterCode;
import java.util.List;

/**
 * Created by chenshaojie on 2017/9/24,9:13.
 */
public class Main {

    public static void main (String[] args) {
        LexicalParser parser = new LexicalParser("test.cmm");
        parser.getSourceCode();
        GrammaticalParser grammaticalParser = new GrammaticalParser(parser);
        grammaticalParser.startParse();
        Generator generator = new Generator(grammaticalParser);
        generator.startGenerate();

        List<InterCode> codes = generator.getCodes();
        //打印中间代码
        for (int i = 0;i<codes.size();i++) {
            System.out.println(i+"  "+codes.get(i).toString());
        }

        Interpreter interpreter = new Interpreter(codes);

        interpreter.run();

    }
}
