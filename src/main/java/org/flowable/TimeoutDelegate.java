package org.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.Map;


/**
 * {@code CallExternalSystemDelegate} is a Java delegate representig the
 * external system.
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106"})
public class TimeoutDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        //获得事务id
        String getEventName = execution.getEventName();
        Map<String, Object> vars = execution.getVariables();
        System.out.println("TimeoutDelegate getEventName " + getEventName + ",vars =  " + vars);
    }
}
