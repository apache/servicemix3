package org.servicemix.common.xbean;

import org.servicemix.common.ServiceUnit;
import org.xbean.kernel.Kernel;

import javax.jbi.JBIException;

public class XBeanServiceUnit extends ServiceUnit {

    private Kernel kernel;

    /**
     * @return Returns the kernel.
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * @param kernel The kernel to set.
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    /* (non-Javadoc)
     * @see org.servicemix.common.ServiceUnit#shutDown()
     */
    public void shutDown() throws JBIException {
        kernel.destroy();
        super.shutDown();
    }
    
}
