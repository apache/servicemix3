package org.servicemix.components.reflection;

public class SimpleCounter implements Counter {

    int counter;
    
    public void increment() {
        counter++;        
    }

    public void decrement() {
        counter--;
    }
    
    int getValue() {
        return counter;
    }

}
