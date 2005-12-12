package org.servicemix.jbi.container;



/**
 * Used to hold a Server Unit configuration.  The components
 * are registered into the JBI container using the Service Unit
 * Manager life cycle methods.
 *
 * @org.xbean.XBean element="serviceunit" rootElement="true"
 *                  description="A deployable service unit container"
 * @version $Revision$
 */
public class SpringServiceUnitContainer {
    
    private ActivationSpec[] activationSpecs;

    public ActivationSpec[] getActivationSpecs() {
        return activationSpecs;
    }

    public void setActivationSpecs(ActivationSpec[] activationSpecs) {
        this.activationSpecs = activationSpecs;
    }

}
