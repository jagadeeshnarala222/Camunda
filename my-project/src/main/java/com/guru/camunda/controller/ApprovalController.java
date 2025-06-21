package com.guru.camunda.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class ApprovalController {

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RuntimeService runtimeService;

	@PostMapping("/start")
	public ResponseEntity<Object> startProcess(@RequestParam("processDefinitionKey") String processDefinitionKey,
			@RequestBody(required = false) Map<String, Object> variables) {

		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
		JSONObject response = new JSONObject();
		response.put("iid", processInstance.getProcessInstanceId().toString());
		response.put("status", "Success");
		return new ResponseEntity<Object>(response.toString(), HttpStatus.OK);
	}

	@GetMapping("/status")
	public ResponseEntity<Object> getProcessStatus(@RequestParam("processInstanceId") String processInstanceId) {
		ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
				.singleResult();
		JSONObject response = new JSONObject();
		response.put("iid", processInstanceId);
		if (instance != null) {
			response.put("status", "RUNNING");
		} else {
			response.put("status", "COMPLETED");
		}
		return new ResponseEntity<Object>(response.toString(), HttpStatus.OK);
	}

	@GetMapping("/activities")
	public ActivityListResponse getActivityList(@RequestParam("processInstanceId") String processInstanceId) {
		ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
		if (activityInstance == null) {
			throw new RuntimeException("Process instance not found with id: " + processInstanceId);
		}
		List<ActivityInfo> activities = new ArrayList<>();
		collectActivities(activityInstance, activities);
		ActivityListResponse response = new ActivityListResponse();
		response.setProcessInstanceId(processInstanceId);
		response.setActivities(activities);
		return response;
	}

	private void collectActivities(ActivityInstance instance, List<ActivityInfo> list) {
		// If the current instance has an activity id (it may be a container without
		// one)
		if (instance.getActivityId() != null) {
			ActivityInfo info = new ActivityInfo();
			info.setActivityInstanceId(instance.getId());
			info.setActivityId(instance.getActivityId());
			list.add(info);
		}
		// Recursively collect activities from child instances
		if (instance.getChildActivityInstances() != null) {
			for (ActivityInstance child : instance.getChildActivityInstances()) {
				collectActivities(child, list);
			}
		}
	}

	public static class ActivityListResponse {
		private String processInstanceId;
		private List<ActivityInfo> activities;

		public String getProcessInstanceId() {
			return processInstanceId;
		}

		public void setProcessInstanceId(String processInstanceId) {
			this.processInstanceId = processInstanceId;
		}

		public List<ActivityInfo> getActivities() {
			return activities;
		}

		public void setActivities(List<ActivityInfo> activities) {
			this.activities = activities;
		}
	}

	public static class ActivityInfo {
		private String activityInstanceId;
		private String activityId;

		public String getActivityInstanceId() {
			return activityInstanceId;
		}

		public void setActivityInstanceId(String activityInstanceId) {
			this.activityInstanceId = activityInstanceId;
		}

		public String getActivityId() {
			return activityId;
		}

		public void setActivityId(String activityId) {
			this.activityId = activityId;
		}
	}

}
