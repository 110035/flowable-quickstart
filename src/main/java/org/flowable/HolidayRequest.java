package org.flowable;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.in;

/**
 * {@code HolidayRequest} is responsible for the creation of Flowable
 * Process Engine and for loading a simple HolidayRequest process. .
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106", "squid:S3776"})
public class HolidayRequest {


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
                .addClasspathResource("holiday-request.bpmn20.xml")
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


        //To start the process instance, it requires initial process variables.
        // Typically, the information is collected through a form.
        // In this example, the java.util.Scanner class intakes data
        // from the command line:
        Scanner scanner = new Scanner(in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        //A process instance is started through the RuntimeService.
        // The collected data is passed as a java.util.Map instance,
        // where the key is the identifier that will be used to retrieve the variables.
        // The process instance is started using a key, 'holidayRequest'.
        // This key matches the id attribute that is set in
        // the BPMN 2.0 XML file, 'holiday-request.bpmn20.xml'.
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);

        //starts an instance of the 'holidayRequest' process.
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest",
                        variables);

        //Once the process instance is started, an execution is created and
        // put in the start event. The execution follows the sequence flow
        // to the user task for the manager approval and executes the user
        // task behavior. This behavior will create a task in the database
        // that can be found using queries later on. A user task is in a wait
        // state and the engine will stop executing anything further,
        // returning the API call.

        //To get the actual task list, create a TaskQuery through the
        // TaskService and configure the query to only return the tasks
        // for the managers group:
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(
                "managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");

        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ") " + tasks.get(i).getName() + "(id=" + tasks.get(i).getId() + ")" + "[created at " + sdf.format(tasks.get(i).getCreateTime()) + "]");

        }

        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.parseInt(scanner.nextLine());
        if (taskIndex > tasks.size()) {
            while (true) {
                System.out.println("Incorrect option,Please retry, Which task would you like to complete?");
                taskIndex = Integer.parseInt(scanner.nextLine());
                if (taskIndex <= tasks.size()) break;
            }

        }
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables =
                taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get(
                        "nrOfHolidays") + " of holidays. Do you approve this?");


        boolean approved = scanner.nextLine().equalsIgnoreCase("y");
        variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.claim(task.getId(), "manager" );
        taskService.complete(task.getId(), variables);


        //Query Historical data (audit data) to detect how the organization works,
        //or detect bottlenecks etc.
        //query the HistoricService from the ProcessEngine for historical activities
        //for one particular process instance and only finished activities.
        //results are also sorted by end time, i.e., the order of execution
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            Date d = new Date(task.getCreateTime().getTime() + activity.getDurationInMillis());
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds by "+activity.getAssignee()+", runs at " + sdf.format(d));
        }

        InputStream stream = genProcessDiagram(processInstance.getId(), processEngine);
        OutputStream fos = new FileOutputStream("file-new.png");
        // 将输入流is写入文件输出流fos中
        int ch = 0;
        try {
            while ((ch = stream.read()) != -1) {
                fos.write(ch);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            //关闭输入流等（略）
            fos.close();
            stream.close();
        }
        scanner.close();
    }


    private static InputStream genProcessDiagram(String processInstanceId, ProcessEngine processEngine) {
        System.out.println("genProcessDiagram, processInstanceId="+processInstanceId);
        HistoryService historyService = processEngine.getHistoryService();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();


        List<String> highLightedActivities = new ArrayList<String>();

        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).finished().orderByHistoricActivityInstanceStartTime().asc().list();

        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivities.add(activityId);
        }





        HistoricProcessInstance his = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(his.getProcessDefinitionId());

        DefaultProcessDiagramGenerator defaultProcessDiagramGenerator = new DefaultProcessDiagramGenerator();

        List<String> highLightedFlows = new ArrayList<String>();
        //防止图片乱码
        InputStream in = defaultProcessDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivities,
                highLightedFlows, "宋体", "宋体", "宋体", null, 1.0);
        return in;


    }
}
