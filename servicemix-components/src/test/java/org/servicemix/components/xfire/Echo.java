package org.servicemix.components.xfire;

public class Echo {
    
    private int count = 0;
    
    public String echo(String msg) {
        count++;
        return msg;
    }

    protected int getCount() {
        return count;
    }
}
