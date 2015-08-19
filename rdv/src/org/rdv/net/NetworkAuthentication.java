
package org.rdv.net;

public interface NetworkAuthentication {
    public String getUserName();
    public String getPassword();
    public boolean authenticate();
    public String getErrorMessage();
}
