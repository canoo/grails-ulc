/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.canoo.grails.ulc

import com.ulcjava.base.application.event.IRoundTripListener
import com.ulcjava.base.application.event.RoundTripEvent
import com.ulcjava.base.client.ISessionStateListener
import com.ulcjava.base.client.UISession
import com.ulcjava.container.local.server.DefaultLocalContainerAdapter
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.springframework.context.ApplicationContext

/**
 * @author ulcteam
 */
public class UlcGrailsRunner {
    static start(appCtx) {
        def runner = runApp()
        StandaloneSessionStateListener listener = new StandaloneSessionStateListener()
        runner.getClientSession().addSessionStateListener(listener)
        // runner.getServerSession().addRoundTripListener new GrailsInitializerForLocalUlcApp(appCtx: appCtx)
        synchronized (listener) {
            listener.wait()
        }
    }

    private static def runApp() {
        return DefaultLocalContainerAdapter.launch()
    }
}

/**
 * @author ulcteam
 */
class StandaloneSessionStateListener implements ISessionStateListener {
    void sessionEnded(UISession session) throws Exception {
        println("Application shutdown ... cleaning up")
        synchronized (this) {
            notifyAll()
        }
    }

    void sessionError(UISession session, Throwable reason) {
        println("Application error..." + reason.getMessage())
    }

    void sessionStarted(UISession session) throws Exception {
        println("Application started...")
    }
}

/**
 * @author ulcteam
 */
class GrailsInitializerForLocalUlcApp implements IRoundTripListener {
    ApplicationContext appCtx

    public void roundTripDidStart(RoundTripEvent event) {
        def listeners = appCtx.getBeansOfType(PersistenceContextInterceptor)
        listeners.each {k, v ->
            v.init()
        }
    }

    public void roundTripWillEnd(RoundTripEvent event) {
        def listeners = appCtx.getBeansOfType(PersistenceContextInterceptor)
        listeners.each {k, v ->
            v.flush()
            v.destroy()
        }
    }
}
