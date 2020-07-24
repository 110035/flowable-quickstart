package org.flowable;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import util.ProcessDiagram;

import java.io.IOException;
import java.util.List;

/**
 * {@code HolidayRequest} is responsible for the creation of Flowable
 * Process Engine and for loading a simple HolidayRequest process. .
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106", "squid:S3776"})
public class SubProcessRequest {


    public static void main(String[] args) throws IOException {
        //creates the Process Engine using a memory-based h2 embedded database
        ProcessEngineConfiguration cfg =
                new StandaloneProcessEngineConfiguration()
                        .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                        .setJdbcUsername("sa")
                        .setJdbcPassword("")
                        .setJdbcDriver("org.h2.Driver")
                        .setDatabaseSchemaUpdate(
                                ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                        .setMailServerHost("mail.test.b2c.srv").setMailServerPort(25);

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
                .addClasspathResource("sub-process.bpmn20.xml")
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
        // 获取taskService对象 Get the first task
        TaskService taskService = processEngine.getTaskService();


        ProcessInstance process = runtimeService.startProcessInstanceByKey("XMSubProcessUS");
        Task task = taskService.createTaskQuery().processInstanceId(process.getId()).singleResult();
        System.out.println("1 task is="+task);
        if (task!=null) {
            System.out.println("提交 task is="+task.getName());
            taskService.complete(task.getId());
        }
        task = taskService.createTaskQuery().processInstanceId(process.getId()).singleResult();
        System.out.println("2 taks is="+task);
        if (task!=null) {
            System.out.println("提交 task is="+task.getName());
            taskService.complete(task.getId());
        }

        task = taskService.createTaskQuery().processInstanceId(process.getId()).singleResult();
        System.out.println("3 task is="+task);
        if (task!=null) {
            System.out.println("提交 task is="+task.getName());
            taskService.complete(task.getId());
        }
        task = taskService.createTaskQuery().processInstanceId(process.getId()).singleResult();
        System.out.println("task is="+task);

        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(process.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds by " + activity.getAssignee() );
        }




        ProcessDiagram.genProcessDiagram(process.getId(), processEngine,"sub-process.png");


    }



}
