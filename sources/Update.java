public class Update{
    
    public enum State{
        UPDATE,
        TIMEOUT,
        EXCEPTION,
        CLEAN
    }
    
    private final long bytesSent;
    private final long bytesReceived;
    private final State state;

    public Update(long bytesSent, long bytesReceived, State state){
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.state = state;
    }
    
    public long getBytesSent(){
        return bytesSent;
    }
    
    public long getBytesReceived(){
        return bytesReceived;
    }
    
    public State getState(){
        return state;
    }
}