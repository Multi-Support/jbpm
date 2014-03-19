/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.services.task.audit;

import java.util.Date;
import org.jbpm.services.task.audit.impl.model.GroupAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.HistoryAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.services.task.audit.impl.model.UserAuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

/**
 *
 */
public class JPATaskLifeCycleEventListener implements TaskLifeCycleEventListener {

    public JPATaskLifeCycleEventListener() {
    }


    protected <T> T persist(TaskPersistenceContext context, T object) {
        return context.persist(object);
    }

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext, new TaskEventImpl(ti.getId(),
            org.kie.internal.task.api.model.TaskEvent.TaskEventType.STARTED,
            userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        if (task != null) {
            task.setStatus(ti.getTaskData().getStatus().name());
            persist(persistenceContext,task);
        }

    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.ACTIVATED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.CLAIMED, userId, new Date()));
        GroupAuditTaskImpl task = persistenceContext.find(GroupAuditTaskImpl.class, ti.getId());
        if (task != null) {
            persistenceContext.remove(task);
        }
        persist(persistenceContext,new UserAuditTaskImpl(userId, ti.getId(), ti.getTaskData().getStatus().name(),
                ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(),
                (!ti.getDescriptions().isEmpty()) ? ti.getDescriptions().get(0).getText() : "", ti.getPriority(),
                (ti.getTaskData().getCreatedBy() == null) ? "" : ti.getTaskData().getCreatedBy().getId(),
                ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                ti.getTaskData().getParentId()));
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.SKIPPED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.STOPPED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.COMPLETED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        if (task != null) {
            persistenceContext.remove(task);
            HistoryAuditTaskImpl historyAuditTaskImpl = new HistoryAuditTaskImpl(task.getActualOwner(), task.getTaskId(), ti.getTaskData().getStatus().name(),
                                                                                task.getActivationTime(), task.getName(),
                                                                                task.getDescription(), task.getPriority(),
                                                                                task.getCreatedBy(), task.getCreatedOn(), 
                                                                                task.getDueDate(), task.getProcessInstanceId(), 
                                                                                task.getProcessId(), task.getProcessSessionId(),
                                                                                task.getParentId());
            persist(persistenceContext,historyAuditTaskImpl);
        }
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.FAILED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
            persist(persistenceContext,new UserAuditTaskImpl(userId, ti.getId(), ti.getTaskData().getStatus().name(),
                    ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(),
                    (!ti.getDescriptions().isEmpty()) ? ti.getDescriptions().get(0).getText() : "", ti.getPriority(),
                    (ti.getTaskData().getCreatedBy() == null) ? "" : ti.getTaskData().getCreatedBy().getId(),
                    ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                    ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                    ti.getTaskData().getParentId()));
        } else if (!ti.getPeopleAssignments().getPotentialOwners().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (OrganizationalEntity o : ti.getPeopleAssignments().getPotentialOwners()) {
                sb.append(o.getId()).append("|");
            }
            persist(persistenceContext,new GroupAuditTaskImpl(sb.toString(), ti.getId(), ti.getTaskData().getStatus().name(),
                    ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(),
                    (!ti.getDescriptions().isEmpty()) ? ti.getDescriptions().get(0).getText() : "", ti.getPriority(),
                    (ti.getTaskData().getCreatedBy() == null) ? "" : ti.getTaskData().getCreatedBy().getId(),
                    ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                    ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                    ti.getTaskData().getParentId()));
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.ADDED, userId, new Date()));
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.EXITED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.RELEASED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        if (task != null) {
            persistenceContext.remove(task);
        }
        StringBuilder sb = new StringBuilder();
        for (OrganizationalEntity o : ti.getPeopleAssignments().getPotentialOwners()) {
            sb.append(o.getId()).append("|");
            
        }
        persist(persistenceContext,new GroupAuditTaskImpl(sb.toString(), ti.getId(), ti.getTaskData().getStatus().name(),
                ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(),
                (!ti.getDescriptions().isEmpty()) ? ti.getDescriptions().get(0).getText() : "", ti.getPriority(),
                (ti.getTaskData().getCreatedBy() == null) ? "" : ti.getTaskData().getCreatedBy().getId(),
                ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                ti.getTaskData().getParentId()));

    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.RESUMED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.SUSPENDED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.FORWARDED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persist(persistenceContext,task);
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persist(persistenceContext,new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.DELEGATED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        if (task != null) {
            persistenceContext.remove(task);
        }
        StringBuilder sb = new StringBuilder();
        for (OrganizationalEntity o : ti.getPeopleAssignments().getPotentialOwners()) {
            sb.append(o.getId());
        }
        persist(persistenceContext,new GroupAuditTaskImpl(sb.toString(), ti.getId(), ti.getTaskData().getStatus().name(),
                ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(),
                (!ti.getDescriptions().isEmpty()) ? ti.getDescriptions().get(0).getText() : "", ti.getPriority(),
                (ti.getTaskData().getCreatedBy() == null) ? "" : ti.getTaskData().getCreatedBy().getId(),
                ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                ti.getTaskData().getParentId()));
    }

    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {

    }

}