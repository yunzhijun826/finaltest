package Tokenizer;

import error.TokenizeError;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;



public class TokenizerTest {
    private Tokenizer init(){
        File file = new File("/Users/wzy/Desktop/c0-compiler/Analysetest.txt");
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringIter it = new StringIter(sc);
        Tokenizer tokenizer = new Tokenizer(it);
        return tokenizer;
    }

    @Test
    public void TestlexUInt() throws TokenizeError {
        Tokenizer tokenizer = init();
        Token t=null;
        do{
             t = tokenizer.nextToken();
            System.out.println(t+" "+t.getEndPos());
        }while(t.getTokenType() != TokenType.EOF);

    }

}
