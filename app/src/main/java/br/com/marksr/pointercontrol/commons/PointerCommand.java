package br.com.marksr.pointercontrol.commons;

public interface PointerCommand {

    void process(String message);

    String execute();

    char getType();
    
    int getPriority();
    
    boolean isDebug();
}

