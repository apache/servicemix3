/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.bpe.timer;

import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.bpe.BPEComponent;

import org.apache.ode.bped.EventDirector;
import org.apache.ode.event.ITimerEvent;
import org.apache.ode.timerservice.IBPETimer;

public class BPETimerJdk extends TimerTask implements IBPETimer {

    private static Log log = LogFactory.getLog(BPETimerJdk.class);
    
    private ITimerEvent te;
    
    public BPETimerJdk(ITimerEvent te) {
        this.te = te;
    }

    public Object getId() {
        return te.getProcId();
    }

    public ITimerEvent getTimerEvent() {
        return te;
    }

    public void run() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Timer " + te + " elapsed at " + new Date());
            }
            EventDirector ed = BPEComponent.getInstance().getEventDirector();
            ed.getIInternalEventDirector().sendEvent(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
