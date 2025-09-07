package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BukkitTaskManager extends TaskManager {

    private final Plugin plugin;
    private final boolean folia;
    private final AtomicInteger taskIds = new AtomicInteger();
    private final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public BukkitTaskManager(final Plugin plugin) {
        this.plugin = plugin;
        this.folia = Bukkit.getServer().getName().toLowerCase().contains("folia");
    }

    @Override
    public int repeat(@Nonnull final Runnable runnable, final int interval) {
        if (this.folia) {
            final ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, scheduled -> runnable.run(), interval, interval);
            final int id = this.taskIds.incrementAndGet();
            this.tasks.put(id, task);
            return id;
        }
        return this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, interval, interval);
    }

    @Override
    public int repeatAsync(@Nonnull final Runnable runnable, final int interval) {
        if (this.folia) {
            final ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, runnable, interval * 50L, interval * 50L, TimeUnit.MILLISECONDS);
            final int id = this.taskIds.incrementAndGet();
            this.tasks.put(id, task);
            return id;
        }
        return this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, runnable, interval, interval);
    }

    @Override
    public void async(@Nonnull final Runnable runnable) {
        if (this.folia) {
            Bukkit.getAsyncScheduler().runNow(this.plugin, runnable);
            return;
        }
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    @Override
    public void task(@Nonnull final Runnable runnable) {
        if (this.folia) {
            Bukkit.getGlobalRegionScheduler().execute(this.plugin, runnable);
            return;
        }
        this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }

    @Override
    public void later(@Nonnull final Runnable runnable, final int delay) {
        if (this.folia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, scheduled -> runnable.run(), delay);
            return;
        }
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, runnable, delay);
    }

    @Override
    public void laterAsync(@Nonnull final Runnable runnable, final int delay) {
        if (this.folia) {
            Bukkit.getAsyncScheduler().runDelayed(this.plugin, runnable, delay * 50L, TimeUnit.MILLISECONDS);
            return;
        }
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, runnable, delay);
    }

    @Override
    public void cancel(final int task) {
        if (task != -1) {
            if (this.folia) {
                final ScheduledTask scheduledTask = this.tasks.remove(task);
                if (scheduledTask != null) {
                    scheduledTask.cancel();
                    return;
                }
            }
            Bukkit.getScheduler().cancelTask(task);
        }
    }

}
