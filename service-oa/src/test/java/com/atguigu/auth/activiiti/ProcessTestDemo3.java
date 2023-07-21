package com.atguigu.auth.activiiti;

import net.minidev.json.JSONUtil;


import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest

public class ProcessTestDemo3 {
    //注入RepositoryService
    @Autowired
    private RepositoryService repositoryService;
    //注入RuntimeService
    @Autowired
    private RuntimeService runtimeService;
    //注入TaskService
    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    //部署及启动
    @Test
    public void deployProcess04() {
        // 流程部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban4.bpmn20.xml")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban4");
        System.out.println(processInstance.getId());
    }


    //查询组任务
    @Test
    public void findGroupTaskList() {
        //查询组任务
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("tom1")//根据候选人查询
                .list();
        for (Task task : list) {
            System.out.println("----------------------------");
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

//拾取组任务
    @Test
    public void claimTask(){
        //拾取任务,即使该用户不是候选人也能拾取(建议拾取时校验是否有资格)
        //校验该用户有没有拾取任务的资格
        Task task = taskService.createTaskQuery()
                .taskCandidateUser("tom1")//根据候选人查询
                .singleResult();
        if(task!=null){
            //拾取任务
            taskService.claim(task.getId(), "tom1");
            System.out.println("任务拾取成功");
        }
    }


    //查询个人待办任务
    @Test
    public void findGroupPendingTaskList() {
        //任务负责人
        String assignee = "tom1";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)//只查询该任务负责人的任务
                .list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }



    //个人任务办理
    @Test
    public void completGroupTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("tom1")  //要查询的负责人
                .singleResult();//返回一条
        taskService.complete(task.getId());
    }


    //归还组任务
    // 如果个人不想办理该组任务，可以归还组任务，归还后该用户不再是该任务的负责人
    @Test
    public void assigneeToGroupTask() {
        String taskId = "d96c3f28-825e-11ed-95b4-7c57581a7819";
        // 任务负责人
        String userId = "tom1";
        // 校验userId是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if (task != null) {
            // 如果设置为null，归还组任务,该 任务没有负责人
            taskService.setAssignee(taskId, null);
        }
    }


    //任务交接，任务负责人将任务交给其它候选人办理该任务
    @Test
    public void assigneeToCandidateUser() {
        // 当前待办任务
        String taskId = "d96c3f28-825e-11ed-95b4-7c57581a7819";
        // 校验zhangsan01是否是taskId的负责人，如果是负责人才可以归还组任务
        Task task = taskService
                .createTaskQuery()
                .taskId(taskId)
                .taskAssignee("tom1")
                .singleResult();
        if (task != null) {
            // 将此任务交给其它候选人zhangsan02办理该 任务
            taskService.setAssignee(taskId, "tom2");
        }
    }




}
