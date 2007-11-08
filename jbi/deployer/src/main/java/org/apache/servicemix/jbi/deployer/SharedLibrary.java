package org.apache.servicemix.jbi.deployer;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Nov 8, 2007
 * Time: 1:01:27 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SharedLibrary {

    String getName();

    String getDescription();

    String getVersion();

    ClassLoader createClassLoader();
    
}
