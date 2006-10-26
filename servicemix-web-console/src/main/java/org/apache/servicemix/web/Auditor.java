package org.apache.servicemix.web;

import java.net.URI;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.audit.AuditorMBean;
import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;

public class Auditor {

    private AuditorMBean mbean;
    private int page;

    public Auditor(AuditorMBean mbean) {
        this.mbean = mbean;
    }
    
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    
    public int getCount() throws AuditorException {
        int count = mbean.getExchangeCount();
        System.err.println(count);
        return count;
    }

    public List<ExchangeInfo> getExchanges() throws AuditorException {
        MessageExchange[] exchanges = mbean.getExchanges(page * 10, Math.min((page + 1) * 10, getCount()));
        ExchangeInfo[] infos = prepare(exchanges);
        return Arrays.asList(infos);
    }

    private ExchangeInfo[] prepare(MessageExchange[] exchanges) {
        ExchangeInfo[] infos = new ExchangeInfo[exchanges.length];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = new ExchangeInfo();
            infos[i].id = exchanges[i].getExchangeId();
            infos[i].mep = getMep(exchanges[i]);
            infos[i].status = exchanges[i].getStatus().toString();
            Object c = exchanges[i].getProperty(JbiConstants.DATESTAMP_PROPERTY_NAME);
            if (c instanceof Calendar) {
                infos[i].date = DateFormat.getDateTimeInstance().format(((Calendar) c).getTime());
            } else if (c instanceof Date) {
                infos[i].date = DateFormat.getDateTimeInstance().format((Date) c);
            }
        }
        return infos;
    }
    
    private String getMep(MessageExchange exchange) {
        URI uri = exchange.getPattern();
        if (MessageExchangeSupport.IN_ONLY.equals(uri)) {
            return "In Only";
        } else if (MessageExchangeSupport.IN_OPTIONAL_OUT.equals(uri)) {
            return "In Opt Out";
        } else if (MessageExchangeSupport.IN_OUT.equals(uri)) {
            return "In Out";
        } else if (MessageExchangeSupport.ROBUST_IN_ONLY.equals(uri)) {
            return "Robust In Only";
        } else {
            return uri.toString();
        }
    }

    public static class ExchangeInfo {
        private String id;
        private String date;
        private String mep;
        private String status;
        
        /**
         * @return Returns the dateStamp.
         */
        public String getDate() {
            return date;
        }
        /**
         * @param dateStamp The dateStamp to set.
         */
        public void setDate(String dateStamp) {
            this.date = dateStamp;
        }
        /**
         * @return Returns the status.
         */
        public String getStatus() {
            return status;
        }
        /**
         * @param status The status to set.
         */
        public void setStatus(String status) {
            this.status = status;
        }
        /**
         * @return Returns the id.
         */
        public String getId() {
            return id;
        }
        /**
         * @param id The id to set.
         */
        public void setId(String id) {
            this.id = id;
        }
        /**
         * @return Returns the mep.
         */
        public String getMep() {
            return mep;
        }
        /**
         * @param mep The mep to set.
         */
        public void setMep(String mep) {
            this.mep = mep;
        }
    }
    
}
