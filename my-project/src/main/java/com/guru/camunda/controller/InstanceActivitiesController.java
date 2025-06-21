package com.guru.camunda.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class InstanceActivitiesController {

	@Autowired
    private HistoryService historyService;

    @GetMapping("/activities/history")
    public HistoricActivityListResponse getHistoricActivities(@RequestParam("processInstanceId") String processInstanceId) {
        List<HistoricActivityInstance> historicActivities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        List<HistoricActivityInfo> activities = historicActivities.stream().map(activity -> {
            HistoricActivityInfo info = new HistoricActivityInfo();
            info.setActivityId(activity.getActivityId());
            info.setActivityName(activity.getActivityName());
            info.setActivityType(activity.getActivityType());
            info.setStartTime(activity.getStartTime());
            info.setEndTime(activity.getEndTime());
            //returns absolute execution time
            info.setAbsoluteTime(Duration.between
            		(Instant.parse(activity.getStartTime().toString()), 
            		Instant.parse(activity.getEndTime().toString())).toMillis());
            return info;
        }).collect(Collectors.toList());

        HistoricActivityListResponse response = new HistoricActivityListResponse();
        response.setProcessInstanceId(processInstanceId);
        response.setActivities(activities);
        return response;
    }
    
 // DTO for the response
    public static class HistoricActivityListResponse {
        private String processInstanceId;
        private List<HistoricActivityInfo> activities;

        public String getProcessInstanceId() {
            return processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
        }

        public List<HistoricActivityInfo> getActivities() {
            return activities;
        }

        public void setActivities(List<HistoricActivityInfo> activities) {
            this.activities = activities;
        }
    }

    // DTO to represent a historic activity instance
    public static class HistoricActivityInfo {
        private String activityId;
        private String activityName;
        private String activityType;
        private java.util.Date startTime;
        private java.util.Date endTime;
        private Long absoluteTime;

        public String getActivityId() {
            return activityId;
        }

        public void setActivityId(String activityId) {
            this.activityId = activityId;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String getActivityType() {
            return activityType;
        }

        public void setActivityType(String activityType) {
            this.activityType = activityType;
        }

        public java.util.Date getStartTime() {
            return startTime;
        }

        public void setStartTime(java.util.Date startTime) {
            this.startTime = startTime;
        }

        public java.util.Date getEndTime() {
            return endTime;
        }

        public void setEndTime(java.util.Date endTime) {
            this.endTime = endTime;
        }

		public Long getAbsoluteTime() {
			return absoluteTime;
		}

		public void setAbsoluteTime(Long absoluteTime) {
			this.absoluteTime = absoluteTime;
		}
        

    }

}
