package org.flowable;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sunjianfei on 2020/7/20.
 */
public class MultiInstancesRequest {

    public static void main(String[] args) throws IOException {
        //creates the Process Engine using a memory-based h2 embedded database
        ProcessEngineConfiguration cfg =
                new StandaloneProcessEngineConfiguration()
                        .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                        .setJdbcUsername("sa")
                        .setJdbcPassword("")
                        .setJdbcDriver("org.h2.Driver")
                        .setDatabaseSchemaUpdate(
                                ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();

        //displays the Process Engine configuration and Flowable version.
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println(
                "ProcessEngine [" + pName + "] Version: [" + ver + "]");

        //loads the supplied holiday-request.bpmn20.xml BPMN model and deploys
        // it to Activiti Process Engine.
        RepositoryService
                repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("mutilInstances-request.bpmn20.xml")
                .deploy();

        //retrieves the deployed model, verififying the process definition is
        //known to the process engine. This is done by creating a new ProcessDefinitionQuery
        // object through the RepositoryService.
        ProcessDefinition
                processDefinition =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
        System.out.println(
                "Found process definition ["
                        + processDefinition.getName() + "] with id ["
                        + processDefinition.getId() + "]");

        RuntimeService runtimeService = processEngine.getRuntimeService();
        HashMap<String, Object> map = new HashMap<>();
        //定义的人员列表4人
        String[] v = { "shareniu1", "shareniu2", "shareniu3", "shareniu4" };
        map.put("per", "bbb");
        map.put("money", "1111");
        map.put("assigneeList", Arrays.asList(v));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("living",map);

        TaskService taskService = processEngine.getTaskService();

        List<Task> list = taskService.createTaskQuery().processInstanceId(pi.getId()).active().list();
        System.out.println("there are " + list.size() + " actived tasks");

        boolean allTaskFinished = taskService.createTaskQuery()
                .processInstanceId(list.get(0).getProcessInstanceId())
                .taskDefinitionKey(list.get(0).getTaskDefinitionKey())
                .active()
                .list().size() == 0;
        System.out.println("all task get finished? " + allTaskFinished);



        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("shareniu4").list();
        System.out.println("shareniu4 have " + tasks.size() + " tasks:");
        taskService.claim(tasks.get(0).getId(),"shareniu4");
        taskService.complete(tasks.get(0).getId());


         allTaskFinished = taskService.createTaskQuery()
                .processInstanceId(list.get(0).getProcessInstanceId())
                .taskDefinitionKey(list.get(0).getTaskDefinitionKey())
                .active()
                .list().size() == 0;
        System.out.println("all task get finished? " + allTaskFinished);














    }
}
