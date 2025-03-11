package joserodpt.realmines.api.managers;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

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
