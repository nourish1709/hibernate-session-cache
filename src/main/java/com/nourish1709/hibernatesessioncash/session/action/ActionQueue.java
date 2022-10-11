package com.nourish1709.hibernatesessioncash.session.action;

import java.util.LinkedList;
import java.util.Queue;

public class ActionQueue {

    // FIFO-manner data structure that represents actions
    private final Queue<EntityAction> entityActionQueue = new LinkedList<>();


    public void addAction(EntityAction entityAction) {
        entityActionQueue.add(entityAction);
    }

    public void executeAll() {
        for (EntityAction action = entityActionQueue.poll(); action != null; action = entityActionQueue.poll()) {
            action.executeAction();
        }
    }
}
