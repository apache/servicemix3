/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.bpe.event.ITimerEvent;
import org.apache.ode.bpe.timerservice.IBPETimer;
import org.apache.ode.bpe.timerservice.IBPETimerService;
import org.apache.ode.bpe.util.BPEProperties;
import org.apache.ode.bpe.util.BPException;
import org.apache.servicemix.bpe.BPEEndpoint;

public class BPETimerServiceJdk implements IBPETimerService {

    private static final Log log = LogFactory.getLog(BPETimerServiceJdk.class);
    private static Timer timer;
    
    public BPETimerServiceJdk() {
        super();
    }

    public IBPETimer createTimer(long startDuration, ITimerEvent timerEvent) throws BPException {
        if (log.isDebugEnabled()) {
            log.debug("Schedule timer " + timerEvent + " for " + startDuration);
        }
        BPETimerJdk tt = new BPETimerJdk(timerEvent, BPEEndpoint.getCurrent());
        timer.schedule(tt, startDuration);
        return tt;
    }

    public IBPETimer createTimer(Date startTime, ITimerEvent timerEvent) throws BPException {
        if (log.isDebugEnabled()) {
            log.debug("Schedule timer " + timerEvent + " at " + startTime);
        }
        BPETimerJdk tt = new BPETimerJdk(timerEvent, BPEEndpoint.getCurrent());
        timer.schedule(tt, startTime);
        return tt;
    }

    public void removeTimer(IBPETimer timer) throws BPException {
        if (log.isDebugEnabled()) {
            log.debug("Timer " + timer.getTimerEvent() + " cancelled");
        }
        ((BPETimerJdk) timer).cancel();

    }

    public void init(BPEProperties props) throws BPException {
        synchronized (BPETimerServiceJdk.class) {
            if (timer == null) {
                timer = new Timer();
            }
        }
    }

}
