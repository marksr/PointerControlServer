package br.com.marksr.pointercontrol.commons;

public class PointerCommandAdapter implements PointerCommand {
    private char mType;
    private String result = null;
    private boolean processDone = false;
    private boolean executeDone = false;
    private int mPriority = 1;
    private boolean mDebug = false;

    public PointerCommandAdapter() {
    }

    public PointerCommandAdapter(String result) {
        this.result = result;
    }

    public PointerCommandAdapter(char type) {
        mType = type;
    }
    
    public PointerCommandAdapter(String result, int priority) {
        this.result = result;
        mPriority = priority;
    }

    public PointerCommandAdapter(char type, int priority) {
        mType = type;
        mPriority = priority;
    }
    
    public void setDebug(boolean value) {
        mDebug = value;
    }

    public char getMessageType() {
        return mType;
    }

    public void setMessageType(char mType) {
        this.mType = mType;
    }

    public boolean isExecuteDone() {
        return executeDone;
    }

    public void setExecuteDone(boolean executeDone) {
        this.executeDone = executeDone;
    }

    public boolean isProcessDone() {
        return processDone;
    }

    public void setProcessDone(boolean processDone) {
        this.processDone = processDone;
    }

    public void reset() {
        setProcessDone(false);
        setExecuteDone(false);
    }

    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    @Override
    public void process(String message) {
        setProcessDone(true);
    }

    @Override
    public String execute() {
        setExecuteDone(true);
        return result;
    }

    @Override
    public char getType() {
        return mType;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public boolean isDebug() {
        return mDebug;
    }
}
