package joserodpt.realmines.api.managers;

import joserodpt.realmines.api.mine.task.MineResetTask;

import java.util.List;

public abstract class MineResetTasksManagerAPI {
    public abstract void addTask(String name, Integer i);

    public abstract void loadTasks();

    public abstract MineResetTask getTask(String name);

    public abstract List<String> getRegisteredTasks();

    public abstract void removeTask(MineResetTask mrt);

    public abstract List<MineResetTask> getTasks();
}
