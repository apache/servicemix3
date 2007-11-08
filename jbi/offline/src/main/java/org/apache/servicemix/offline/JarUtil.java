package org.apache.servicemix.offline;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.springframework.core.io.Resource;
import org.springframework.osgi.internal.test.util.JarUtils;

public class JarUtil {

    private Manifest manifest; 
    
    public String getJarContent(Resource resource) {
        return JarUtils.dumpJarContent(resource);
    }
    
    public String getManifestContent(Resource resource) {
        manifest = JarUtils.getManifest(resource);
        StringBuffer content = new StringBuffer();
        final String indent = " "; 
        
        Map entries = manifest.getEntries();
        
        for (Iterator iter0 = (Iterator) entries.keySet().iterator(); iter0.hasNext();) {
            String entryName = (String) iter0.next();
            content.append(entryName);
            Attributes attrs = (Attributes) entries.get(entryName);
            
            for (Iterator iter1 = (Iterator) attrs.keySet().iterator(); iter1.hasNext();) {
                Attributes.Name name = (Attributes.Name) iter1.next();
                String attr = attrs.getValue(name);
                content.append(indent + attr);
            }
        }
        
        return content.toString();
    }

}
