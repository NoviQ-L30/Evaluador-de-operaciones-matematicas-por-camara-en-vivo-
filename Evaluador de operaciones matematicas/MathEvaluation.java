package com.example.escanermult;

public class MathEvaluation {
    private String expression;
    private int ch,pos=-1;
    private boolean wrongResult=false;

    public MathEvaluation(String expression){
        this.expression=expression;
    }

    public String parse(){
        nextChar();
        double x = parseLowArithmetic();
        if(pos<expression.length())
            wrongResult=true;
        if(!wrongResult)
            return String.valueOf(x);
        else return "Expresion equivocada";

    }

    private double parseLowArithmetic() {
        double x = parseMediumArithmetic();
        for (; ; ) {
            if (evaluate('+'))
                x += parseMediumArithmetic();
            else if (evaluate('-'))
                x -= parseMediumArithmetic();
            else
                return x;
        }
    }

    private double parseMediumArithmetic(){
        double x = parseHighArithmetic();
        for(;;){
            if (evaluate('*'))
                x*= parseHighArithmetic();
            else if (evaluate('/'))
                x/= parseHighArithmetic();
            else if (evaluate('%'))
                x%= parseHighArithmetic();
            else
                return x;
        }
    }

    private double parseHighArithmetic(){
        if(evaluate('-')){
            return -parseHighArithmetic();
        }

        int startPos=this.pos;
        double x=0;
        if (evaluate('(')){
            x=parseLowArithmetic();
            evaluate(')');
        }
        else if((ch>='0' && ch<='9') || ch == '.'){
            while((ch>='0' && ch<='9' ) || ch=='.')
                nextChar();
            x= Double.parseDouble(expression.substring(startPos,this.pos));
        } else if (ch>='a' && ch<='z') {
            while (ch>='a' && ch<='z')
                nextChar();
            String func=expression.substring(startPos,this.pos);
            x=parseHighArithmetic();

            switch (func){
                case "sqrt":
                    x = Math.sqrt(x);
                    break;
                case "sin":
                    x = Math.sin(Math.toRadians(x));
                    break;
                case "cos":
                    x= Math.cos(Math.toRadians(x));
                    break;
                case "tan":
                    x= Math.tan(Math.toRadians(x));
                    break;
                default:
                    wrongResult=true;
                    break;
            }
        }else wrongResult=true;
        if (evaluate('^'))
            x=Math.pow(x,parseHighArithmetic());
        return x;
    }

    private void nextChar(){
        ch=(++pos <expression.length())?expression.charAt(pos):-1;
    }

    private boolean evaluate(int charToEvaluate){
        while (ch==' ')
            nextChar();
        if(ch== charToEvaluate){
            nextChar();
            return true;
        }
        return false;
    }

}

