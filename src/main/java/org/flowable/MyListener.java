package org.flowable;

import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.task.IdentityLink;

import java.util.Map;
import java.util.Set;

/**
 * Created by sunjianfei on 2020/7/15.
 */
public class MyListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {

        String processInstanceId = delegateTask.getProcessInstanceId();
        delegateTask.getExecution().setVariable("Hello","World");
        Map<String, Object> vars = delegateTask.getExecution().getVariables();
        System.out.println("Listener trigger " + processInstanceId + ",eventname=" + delegateTask.getEventName());
        System.out.println("vars =  " + vars);


        // 获取配置的表达式
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        System.out.println("assignExpression =  " + candidates);




    }
}