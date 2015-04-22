package org.ecocean.util;

public class LogBuilder {
    private StringBuilder builder;
    
    public LogBuilder()
    {
        builder = new StringBuilder();
    }
    
    public LogBuilder(final String initialMsg)
    {
        builder = new StringBuilder(initialMsg);
    }

    public static String quickLog(final String variable,
                                  final String value)
    {
        return new LogBuilder().appendVar(variable, value).toString();
    }

    public static String quickLog(final String variable,
                                  final int value)
    {
        return quickLog(variable, String.valueOf(value));
    }

    public static String quickLog(final String variable,
                                  final boolean value)
    {
        return quickLog(variable, String.valueOf(value));
    }
    
    
    public LogBuilder append(final String value)
    {
        builder.append(value);
        return this;
    }
    
    public LogBuilder appendVar(final String variable,
                                final String value)
    {
        builder.append(variable).append(" [").append(value).append("]");
        
        return this;
    }
    
    public LogBuilder appendVar(final String variable,
                                final int value)
    {
        return appendVar(variable, String.valueOf(value));
    }
    
    @Override
    public String toString()
    {
        return builder.toString();
    }
}
