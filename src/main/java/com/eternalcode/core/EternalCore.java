/*
 * Copyright (c) 2022. EternalCode.pl
 */

package com.eternalcode.core;

import com.eternalcode.core.command.binds.MessageAction;
import com.eternalcode.core.command.binds.MessageActionArgument;
import com.eternalcode.core.command.binds.PlayerArgument;
import com.eternalcode.core.command.binds.PlayerSenderBind;
import com.eternalcode.core.command.implementations.AdminChatCommand;
import com.eternalcode.core.command.implementations.AlertCommand;
import com.eternalcode.core.command.implementations.AnvilCommand;
import com.eternalcode.core.command.implementations.CartographyTableCommand;
import com.eternalcode.core.command.implementations.ChatCommand;
import com.eternalcode.core.command.implementations.ClearCommand;
import com.eternalcode.core.command.implementations.DisposalCommand;
import com.eternalcode.core.command.implementations.EnderchestCommand;
import com.eternalcode.core.command.implementations.EternalCoreCommand;
import com.eternalcode.core.command.implementations.FeedCommand;
import com.eternalcode.core.command.implementations.FlyCommand;
import com.eternalcode.core.command.implementations.GamemodeCommand;
import com.eternalcode.core.command.implementations.GodCommand;
import com.eternalcode.core.command.implementations.GrindstoneCommand;
import com.eternalcode.core.command.implementations.HatCommand;
import com.eternalcode.core.command.implementations.HealCommand;
import com.eternalcode.core.command.implementations.HelpOpCommand;
import com.eternalcode.core.command.implementations.InventoryOpenCommand;
import com.eternalcode.core.command.implementations.KillCommand;
import com.eternalcode.core.command.implementations.ScoreboardCommand;
import com.eternalcode.core.command.implementations.SkullCommand;
import com.eternalcode.core.command.implementations.SpeedCommand;
import com.eternalcode.core.command.implementations.StonecutterCommand;
import com.eternalcode.core.command.implementations.WhoIsCommand;
import com.eternalcode.core.command.implementations.WorkbenchCommand;
import com.eternalcode.core.configuration.ConfigurationManager;
import com.eternalcode.core.configuration.MessagesConfiguration;
import com.eternalcode.core.configuration.PluginConfiguration;
import com.eternalcode.core.listeners.PlayerChatListener;
import com.eternalcode.core.listeners.PlayerJoinListener;
import com.eternalcode.core.listeners.PlayerQuitListener;
import com.eternalcode.core.listeners.inventory.InventoryClickListener;
import com.eternalcode.core.listeners.inventory.InventoryCloseListener;
import com.eternalcode.core.listeners.player.PlayerCommandPreprocessListener;
import com.eternalcode.core.listeners.sign.SignChangeListener;
import com.eternalcode.core.managers.chat.ChatManager;
import com.eternalcode.core.listeners.scoreboard.ScoreboardListener;
import com.eternalcode.core.managers.scoreboard.ScoreboardManager;
import com.eternalcode.core.listeners.user.CreateUserListener;
import com.eternalcode.core.user.UserService;
import com.eternalcode.core.utils.ChatUtils;
import com.google.common.base.Stopwatch;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import panda.std.stream.PandaStream;

import java.util.concurrent.TimeUnit;

@Plugin(name = "EternalCore", version = "1.0.1-APLHA")
@Author("EternalCodeTeam")
@ApiVersion(ApiVersion.Target.v1_17)
@Description("Essential plugin for your server!")
@LogPrefix("EternalCore")

public class EternalCore extends JavaPlugin {

    private static final String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
    @Getter private static EternalCore instance;
    @Getter private UserService userService;
    @Getter private LiteCommands liteCommands;
    @Getter private ConfigurationManager configurationManager;
    @Getter private ScoreboardManager scoreboardManager;
    @Getter private ChatManager chatManager;
    private boolean isPaper = false;

    @Override
    public void onEnable() {
        Stopwatch started = Stopwatch.createStarted();

        this.softwareCheck();

        instance = this;

        this.configurationManager = new ConfigurationManager(this.getDataFolder());
        this.configurationManager.loadAndRenderConfigs();

        this.scoreboardManager = new ScoreboardManager(this, configurationManager);
        this.scoreboardManager.updateTask();

        this.chatManager = new ChatManager(configurationManager.getPluginConfiguration());


        // bStats metrics
        Metrics metrics = new Metrics(this, 13964);
        metrics.addCustomChart(new SingleLineChart("users", () -> 0));

        // Services
        this.userService = new UserService();

        this.liteCommands = LiteBukkitFactory.builder(this.getServer(), "EternalCore")
            .argument(Player.class, new PlayerArgument(configurationManager.getMessagesConfiguration()))
            .argument(MessageAction.class, new MessageActionArgument())
            .bind(Player.class, new PlayerSenderBind(configurationManager.getMessagesConfiguration()))
            .bind(ConfigurationManager.class, () -> configurationManager)
            .bind(MessagesConfiguration.class, () -> configurationManager.getMessagesConfiguration())
            .bind(PluginConfiguration.class, () -> configurationManager.getPluginConfiguration())
            .bind(EternalCore.class, () -> this)
            .bind(UserService.class, () -> userService)
            .command(AdminChatCommand.class,
                AlertCommand.class,
                AnvilCommand.class,
                CartographyTableCommand.class,
                ChatCommand.class,
                ClearCommand.class,
                DisposalCommand.class,
                EnderchestCommand.class,
                FeedCommand.class,
                FlyCommand.class,
                GamemodeCommand.class,
                GodCommand.class,
                GrindstoneCommand.class,
                HatCommand.class,
                HealCommand.class,
                KillCommand.class,
                SkullCommand.class,
                SpeedCommand.class,
                StonecutterCommand.class,
                WhoIsCommand.class,
                WorkbenchCommand.class,
                EternalCoreCommand.class,
                ScoreboardCommand.class,
                AdminChatCommand.class,
                HelpOpCommand.class,
                InventoryOpenCommand.class)
            .register();

        // Register events
        PandaStream.of(
            new PlayerChatListener(chatManager, configurationManager.getMessagesConfiguration(), configurationManager.getPluginConfiguration()),
            new PlayerJoinListener(configurationManager),
            new PlayerQuitListener(configurationManager),
            new CreateUserListener(this),
            new ScoreboardListener(configurationManager),
            new InventoryClickListener(),
            new InventoryCloseListener(),
            new PlayerCommandPreprocessListener(configurationManager.getMessagesConfiguration()),
            new SignChangeListener()
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

        long millis = started.elapsed(TimeUnit.MILLISECONDS);
        this.getLogger().info(ChatUtils.color("&7Successfully loaded EternalCore in " + millis + "ms"));
    }

    @Override
    public void onDisable() {
        this.liteCommands.getPlatformManager().unregisterCommands();

        PluginConfiguration config = configurationManager.getPluginConfiguration();

        config.chatStatue = chatManager.isChatEnabled();
        config.chatSlowMode = chatManager.getChatDelay();
    }

    private void softwareCheck() {
        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            isPaper = true;
        } catch (ClassNotFoundException classNotFoundException) {
            this.getLogger().warning(ChatUtils.color("&c&lYour server running on unsupported software, use paper minecraft software and other paper 1.17x forks"));
            this.getLogger().warning(ChatUtils.color("&c&lDownload paper from https://papermc.io/downloads"));
            this.getLogger().warning(ChatUtils.color("&6&lWARRING&r &6Supported minecraft version is 1.17-1.18x"));
        }

        if (isPaper) {
            this.getLogger().info(ChatUtils.color("&a&lYour server running on supported software, congratulations!"));
            this.getLogger().info(ChatUtils.color("&a&lServer version: &7" + Bukkit.getServer().getVersion()));
        }

        switch (version) {
            case "v1_8_R3", "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1", "v1_14_R1", "v1_15_R1", "v1_16_R1" -> Bukkit.getLogger().info("EternalCore no longer supports your version, be aware that there may be bugs!");
        }
    }
}
