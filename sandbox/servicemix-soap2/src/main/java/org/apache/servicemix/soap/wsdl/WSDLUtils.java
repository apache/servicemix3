package org.apache.servicemix.soap.wsdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPHeaderFault;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12HeaderFault;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.servicemix.soap.api.Fault;

import com.ibm.wsdl.Constants;

public class WSDLUtils {

    private static WSDLFactory wsdl11Factory;
    
    public static WSDLReader createWSDL11Reader() {
        WSDLReader reader = getWSDL11Factory().newWSDLReader();
        reader.setFeature(Constants.FEATURE_VERBOSE, false);
        return reader;
    }
    
    public static WSDLFactory getWSDL11Factory() {
        if (wsdl11Factory == null) {
            try {
                wsdl11Factory = WSDLFactory.newInstance();
            } catch (WSDLException e) {
                throw new Fault(e);
            }
        }
        return wsdl11Factory;
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getParts(SOAPBody body) {
        return (List<String>) body.getParts();
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getParts(SOAP12Body body) {
        return (List<String>) body.getParts();
    }
    
    @SuppressWarnings("unchecked")
    public static Collection<BindingFault> getBindingFaults(BindingOperation bop) {
        return (Collection<BindingFault>) bop.getBindingFaults().values();
    }
    
    @SuppressWarnings("unchecked")
    public static List<SOAPHeaderFault> getSOAPHeaderFaults(SOAPHeader sh) {
        return (List<SOAPHeaderFault>) sh.getSOAPHeaderFaults();
    }
    
    @SuppressWarnings("unchecked")
    public static List<SOAP12HeaderFault> getSOAPHeaderFaults(SOAP12Header sh) {
        return (List<SOAP12HeaderFault>) sh.getSOAP12HeaderFaults();
    }
    
    public static List<ElementExtensible> getElements(BindingOperation bop) {
        List<ElementExtensible> l = new ArrayList<ElementExtensible>();
        l.add(bop);
        l.add(bop.getBindingInput());
        l.add(bop.getBindingOutput());
        l.addAll(getBindingFaults(bop));
        return l;
    }
    
    public static <T> T getExtension(ElementExtensible element, Class<T> format) {
        if (element != null) {
            for (Iterator it = element.getExtensibilityElements().iterator(); it.hasNext();) {
                Object ex = it.next();
                if (format.isInstance(ex)) {
                    return format.cast(ex);
                }
            }
        }
        return null;
    }
    
    public static <T> List<T> getExtensions(ElementExtensible element, Class<T> format) {
        List<T> l = new ArrayList<T>();
        if (element != null) {
            for (Iterator it = element.getExtensibilityElements().iterator(); it.hasNext();) {
                Object ex = it.next();
                if (format.isInstance(ex)) {
                    l.add(format.cast(ex));
                }
            }
        }
        return l;
    }
    
    public static <T> List<T> getExtensions(List<ElementExtensible> elements, Class<T> format) {
        List<T> l = new ArrayList<T>();
        for (ElementExtensible e : elements) {
            l.addAll(getExtensions(e, format));
        }
        return l;
    }
    
}
