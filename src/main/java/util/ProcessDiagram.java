package util;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunjianfei on 2020/7/21.
 */
public class ProcessDiagram {

    public static void genProcessDiagram(String processInstanceId, ProcessEngine processEngine, String filename) throws IOException {
        System.out.println("genProcessDiagram, processInstanceId=" + processInstanceId);
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

        OutputStream fos = new FileOutputStream(filename);
        // 将输入流is写入文件输出流fos中
        int ch = 0;
        try {
            while ((ch = in.read()) != -1) {
                fos.write(ch);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            //关闭输入流等（略）
            fos.close();
            in.close();
        }


    }
}
