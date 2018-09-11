package interpreter.semantic;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chenshaojie on 2017/9/18,16:12.
 */
public class SymbolTable {

    private final HashMap<String,Symbol> map;

    public SymbolTable () {
        this.map = new HashMap<>();
    }

    public void addSymbol(Symbol symbol) {//添加一个符号
        if(!map.containsKey(symbol.getName())) {
            map.put(symbol.getName(),symbol);
        }
        else {
            Symbol previous = map.get(symbol.getName());
            symbol.next= previous;
            map.put(symbol.getName(),symbol);
        }
    }

    public void deleteSymbols(List<String> names) {
        for (String name:names) {
            deleteSymbol(name);
        }
    }

    public void deleteSymbol(String name) {
        Symbol previous = map.get(name);
        map.put(name,previous.next);
    }

    public Symbol getSymbol(String name) {
        return map.get(name);
    }
}
