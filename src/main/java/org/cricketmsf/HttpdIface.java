package org.cricketmsf;

public interface HttpdIface {
    public void run();
    public void stop();
    public boolean isSsl();
}
