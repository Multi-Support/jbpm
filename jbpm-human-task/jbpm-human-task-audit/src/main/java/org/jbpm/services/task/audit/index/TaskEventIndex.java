/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.audit.index;

import org.apache.lucene.document.Document;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.TypeFilter;
import org.kie.internal.task.api.model.TaskEvent;

/**
 * @author Hans Lund
 */
public class TaskEventIndex extends ModelIndexImpl<TaskEvent> {

    private static String type = "TaskEvent";
    private static TypeFilter<TaskEvent> typeFilter = new TypeFilter<TaskEvent>(type);


    @Override
    public Document prepare(TaskEvent object) {
        Document doc = new Document();
        addKeyWordField("ALL", "ALL", doc, false);
        addKeyWordField("type", type , doc ,false);
        addBinary(object,doc);
        addKeyWordField("id", getId(object), doc,false);
        addLongField("taskId", object.getTaskId(), doc, false);
        addKeyWordField("taskEventType", object.getType().name(), doc, false);
        addKeyWordField("userId", object.getUserId(), doc, false);
        addDateField("logTime", object.getLogTime(), doc, false);
        return doc;
    }

    @Override
    public Filter getTypeFilter() {
        return typeFilter;
    }

    @Override
    public String getId(TaskEvent object) {
        return String.valueOf(object.getId());
    }

    @Override
    public Class<TaskEvent> getClazz() {
        return TaskEvent.class;
    }
}
