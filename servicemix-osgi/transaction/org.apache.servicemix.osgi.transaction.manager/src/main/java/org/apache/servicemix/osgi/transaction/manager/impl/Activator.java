package org.apache.servicemix.osgi.transaction.manager.impl;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import javax.transaction.TransactionManager;

import org.apache.geronimo.transaction.log.HOWLLog;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private BundleContext context;
    private HOWLLog transactionLog;
    private TransactionManager transactionManager;

    /**
     * Implements BundleActivator.start(). P
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;
        XidFactory xidFactory = new XidFactoryImpl();
        File serverBaseDir = null;
        List resourceManagers = null;
        transactionLog = new HOWLLog(
                                     getString("bufferClassName", "org.objectweb.howl.log.BlockLogBuffer"), 
                                     getInteger("bufferSize", 32), 
                                     getBoolean("checksumEnabled", true), 
                                     getBoolean("adler32Checksum", true), 
                                     getInteger("flushSleepTimeMilliseconds", 50), 
                                     getString("logFileDir", ""), 
                                     getString("logFileExt", "log"), 
                                     getString("logFileName", "transaction"), 
                                     getInteger("maxBlocksPerFile", -1), 
                                     getInteger("maxBuffers", 0), 
                                     getInteger("maxLogFiles", 2), 
                                     getInteger("minBuffers", 4), 
                                     getInteger("threadsWaitingForceThreshold", -1), 
                                     xidFactory,
                                     null);
        transactionLog.doStart();
        transactionManager = new GeronimoTransactionManager(
                                    getInteger("defaultTransactionTimeoutSeconds", 60),
                                    xidFactory,
                                    transactionLog,
                                    resourceManagers);
        context.registerService(TransactionManager.class.getName(), transactionManager, null);
    }

    /**
     * Implements BundleActivator.stop().
     * 
     * @param context
     *            the framework context for the bundle.
     */
    public void stop(BundleContext context) throws Exception {
        transactionLog.doStop();
        transactionLog = null;
        transactionManager = null;
    }

    protected String getString(String name, String def) {
        String val = context.getProperty("org.apache.servicemix.osgi.transaction.manager." + name);
        if (val == null) {
            return def;
        }
        return val;
    }

    protected int getInteger(String name, int def) {
        String val = context.getProperty("org.apache.servicemix.osgi.transaction.manager." + name);
        if (val == null) {
            return def;
        }
        return Integer.parseInt(val);
    }

    protected boolean getBoolean(String name, boolean def) {
        String val = context.getProperty("org.apache.servicemix.osgi.transaction.manager." + name);
        if (val == null) {
            return def;
        }
        return Boolean.valueOf(val);
    }

}
