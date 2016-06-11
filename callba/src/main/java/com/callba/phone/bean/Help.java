package com.callba.phone.bean;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
public class Help {
    private String ask;
    private String answer;

    public Help( String ask,String answer) {
        this.answer = answer;
        this.ask = ask;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }
}
