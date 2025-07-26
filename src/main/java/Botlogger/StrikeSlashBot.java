package Botlogger;

import Botlogger.commands.StrikeCommandHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cdimascio.dotenv.Dotenv;

public class StrikeSlashBot {
    private static final Logger logger = LoggerFactory.getLogger(StrikeSlashBot.class);

    public static void main(String[] args) {
        Dotenv dontenv = Dotenv.load();

        String token = dontenv.get("STRIKEBOT_TOKEN");
        String guildId = dontenv.get("GUILD_ID");

        StrikeCommandHandler commandHandler = new StrikeCommandHandler();

        JDABuilder.createDefault(token)
                .addEventListeners(commandHandler)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(@NotNull ReadyEvent event) {
                        logger.info("JDA is ready. Updating guild commands...");
                        event.getJDA().getGuildById(guildId)
                                .updateCommands()
                                .addCommands(commandHandler.getCommandData())
                                .queue(
                                        success -> logger.info("Guild commands updated."),
                                        error -> logger.error("Failed to update commands", error)
                                );
                    }
                })
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing("ZR nerdians are bad"))
                .build();

        logger.info("StrikeSlashBot started successfully.");
    }
}