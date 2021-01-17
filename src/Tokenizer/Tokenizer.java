package Tokenizer;


import error.ErrorCode;
import error.TokenizeError;
import util.Pos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    private StringIter it;
    StringBuilder token = new StringBuilder();

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        }
        else if(peek == '\''){
            return lexCHAR();
        }
        else if(peek == '"'){
            return lexSTRING();
        }
        else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        } else if(peek == '_') {
            return lexIdentOrKeyword();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUIntOrDouble() throws TokenizeError{  //判断并返回 无符号整数
        token.setLength(0);
        Pos start = it.currentPos();
        // 请填空：
        while(true) {// 直到查看下一个字符不是数字为止:
            char peek = it.peekChar();
            if(!Character.isDigit(peek)){
                break;
            }
            char now = it.nextChar();
            token.append(now);
        }

        if(it.peekChar() == '.'){
            token.append(it.nextChar());

            while(true) {// 直到查看下一个字符不是数字为止:
                char peek = it.peekChar();
                if(!Character.isDigit(peek)){
                    break;
                }
                char now = it.nextChar();
                token.append(now);
            }

            if(it.peekChar() == 'e' || it.peekChar() == 'E'){ //有后面的一串
                token.append(it.nextChar());

                if(it.peekChar() == '+' || it.peekChar() == '-'){
                    token.append(it.nextChar());
                }

                while(true) {// 直到查看下一个字符不是数字为止:
                    char peek = it.peekChar();
                    if(!Character.isDigit(peek)){
                        break;
                    }
                    char now = it.nextChar();
                    token.append(now);
                }
            }
            double num = Double.valueOf(token.toString());
            Token t = new Token(TokenType.DOUBLE_LITERAL, num, start, it.currentPos());
            return t;
        }

        if(token.length() != 0){
            long num = Long.valueOf(token.toString());
            Token t = new Token(TokenType.UINT_LITERAL, num, start, it.currentPos());
            return t;
        }
        return null;
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
    }

    private Token lexSTRING() throws TokenizeError {  //判断并返回 字符串常量
        token.setLength(0);
        Pos start = it.currentPos();

        it.nextChar();
        while(true){
            if(it.isEOF()){
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            char peek = it.peekChar();



            if(peek == '"'){
                it.nextChar();
                Token t = new Token(TokenType.STRING_LITERAL, token.toString(), start, it.currentPos());
                return t;
            }
            char now = it.nextChar();
//            System.out.println(now+ " 这轮是你！");
            if(now == '\\'){
                lexEscape(now);
            }
            else{
                lexRegular(now);
            }
        }
    }

    private Token lexCHAR() throws TokenizeError {
        token.setLength(0);
        Pos start = it.currentPos();
        Token t = null;

        it.nextChar();
        char peek = it.peekChar();

        if(peek != '\'' && peek != '\\'){
            t = new Token(TokenType.CHAR_LITERAL, it.nextChar(), start, it.currentPos());
        }
        else if(peek == '\\'){
            it.nextChar();
            Pattern p = Pattern.compile("[\\\\\"'nrt]");
            peek = it.peekChar();
            Matcher m = p.matcher(String.valueOf(peek));
            char cur ;

            if(m.matches()) {
                if (peek == '\\') {
                    cur = '\\';
                } else if (peek == 'n') {
                    cur = '\n';
                } else if (peek == 't') {
                    cur = '\t';
                } else if (peek == '"') {
                    cur = '"';
                } else if (peek == 'r') {
                    cur = '\r';
                } else if (peek == '\'') {
                    cur = '\'';
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
                it.nextChar();
                t = new Token(TokenType.CHAR_LITERAL, cur, start, it.currentPos());
                
            }
        }
        else{
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
        if(it.peekChar() != '\''){
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
        it.nextChar();
        return t;
    }

    private void lexEscape(char now) throws TokenizeError {
        Pattern p = Pattern.compile("[\\\\\"'nrt]");
        char peek = it.peekChar();

        Matcher m = p.matcher(String.valueOf(peek));

        if(m.matches()){
            if(peek == '\\'){
                token.append('\\');
            }
            if(peek == 'n'){
                token.append('\n');
            }
            if(peek == 't'){
                token.append('\t');
            }
            if(peek == '"'){
                token.append('"');
            }
            if(peek == 'r'){
                token.append('\r');
            }
            if(peek == '\''){
                token.append('\'');
            }
            it.nextChar();
        }
        else throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
    }

    private void lexRegular(char now) throws TokenizeError {
        Pattern p = Pattern.compile("[^\"\\\\]");
        //char peek = it.peekChar();
        Matcher m = p.matcher(String.valueOf(now));
//        System.out.println(now);
//        System.out.println(m);
        if(m.matches()){
//            it.nextChar();
//            System.out.println("sd");
            token.append(now);
        }
        else throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError{
        String[] keywords = {"FN_KW", "LET_KW", "CONST_KW", "AS_KW", "WHILE_KW", "IF_KW", "ELSE_KW", "RETURN_KW", "BREAK_KW", "CONTINUE_KW"};
        String[] keywordsReal = {"FN", "LET", "CONST", "AS", "WHILE", "IF", "ELSE", "RETURN", "BREAK", "CONTINUE"};

        Pos start = it.currentPos();
        token.setLength(0);

        while(true){
            char peek = it.peekChar();
            if(!Character.isLetterOrDigit(peek) && peek != '_'){
                break;
            }
            char now = it.nextChar();
            token.append(now);
        }
        if(token.length() != 0){
            for(int i = 0; i < keywords.length; i++){
                if(token.toString().toLowerCase().equals(keywordsReal[i].toLowerCase())){
                    Token t = new Token(TokenType.valueOf(keywords[i]), token.toString(), start, it.currentPos());
                    return t;
                }
            }
            Token t = new Token(TokenType.IDENT, token.toString(), start, it.currentPos());
            return t;
        }
        return null;
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串

    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                if(it.peekChar() == '>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());


            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                if(it.peekChar() == '/'){
                    while (it.nextChar() != '\n'){
                        if(it.isEOF()){
                            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                        }
                    }
                    return nextToken();
                }
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            // 填入更多状态和返回语句
            case '=':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());

            case '!':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());

            case '>':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());

            case '<':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }


    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
