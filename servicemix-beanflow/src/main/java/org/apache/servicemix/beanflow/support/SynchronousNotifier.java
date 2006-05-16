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
package org.apache.servicemix.beanflow.support;

import java.util.ArrayList;
import java.util.List;

/**
 * A default {@link Notifier} which just performs the notifications
 * synchronously as the state changes.
 * 
 * @version $Revision: $
 */
public class SynchronousNotifier implements Notifier {
    private List<Runnable> listeners = new ArrayList<Runnable>();

    public void addRunnable(Runnable listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeRunnable(Runnable listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void run() {
        Runnable[] array = null;
        synchronized (listeners) {
            array = new Runnable[listeners.size()];
            listeners.toArray(array);
        }
        for (Runnable listener : array) {
            listener.run();
        }
    }

}
