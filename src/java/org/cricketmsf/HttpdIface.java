package org.cricketmsf;

/**
 *
 * @author greg
 */
public interface HttpdIface {
    public void run();
    public void stop();
    public boolean isSsl();
}
