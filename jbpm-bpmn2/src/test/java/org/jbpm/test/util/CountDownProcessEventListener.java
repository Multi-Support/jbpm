package org.jbpm.test.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionManagerFactory;
import org.drools.persistence.TransactionSynchronization;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CountDownProcessEventListener extends DefaultProcessEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CountDownProcessEventListener.class);
    
    private String nodeName;
    private CountDownLatch latch;
    
    public CountDownProcessEventListener(String nodeName, int threads) {
        this.nodeName = nodeName;
        this.latch = new CountDownLatch(threads);
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        if (nodeName.equals(event.getNodeInstance().getNodeName())) {
            countDown();
        }
    }
    
    public void waitTillCompleted() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.debug("Interrputed thread while waiting for all triggers for node {}", nodeName);
        }
    }
    
    public void waitTillCompleted(long timeOut) {
        try {
            latch.await(timeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.debug("Interrputed thread while waiting for all triggers for node {}", nodeName);
        }
    }
    
    protected void countDown() {
        try {
            TransactionManager tm = TransactionManagerFactory.get().newTransactionManager();
            if (tm != null && tm.getStatus() != TransactionManager.STATUS_NO_TRANSACTION
                    && tm.getStatus() != TransactionManager.STATUS_ROLLEDBACK
                    && tm.getStatus() != TransactionManager.STATUS_COMMITTED) {
                tm.registerTransactionSynchronization(new TransactionSynchronization() {
                    
                    @Override
                    public void beforeCompletion() {        
                    }
                    
                    @Override
                    public void afterCompletion(int status) {
                        latch.countDown();
                    }
                });
            } else {            
                latch.countDown();
            }
        } catch (Exception e) {
            latch.countDown();
        }
    }
}
