package javax.jbi.messaging;

public final class ExchangeStatus
{
    
    public static final ExchangeStatus ACTIVE = new ExchangeStatus("Active");
    
    public static final ExchangeStatus ERROR = new ExchangeStatus("Error");
    
    public static final ExchangeStatus DONE  = new ExchangeStatus("Done");
    
    private String mStatus;
    
    private ExchangeStatus(String status)
    {
        mStatus = status;
    }
    
    public String toString()
    {
        return mStatus;
    }
    
   
    
    public static ExchangeStatus valueOf(String status)
    {
        ExchangeStatus instance;
        
        //
        //  Convert symbolic name to object reference.
        //
        if (status.equals(DONE.toString()))
        {
            instance = DONE;
        }
        else if (status.equals(ERROR.toString()))
        {
            instance = ERROR;
        }
        else if (status.equals(ACTIVE.toString()))
        {
            instance = ACTIVE;
            
        }
        else
        {
            //
            //  Someone has a problem.
            //
            throw new java.lang.IllegalArgumentException(status);
        }
       
        return (instance);
    }
    
    public int hashCode()
    {
        return mStatus.hashCode();
    }
}
