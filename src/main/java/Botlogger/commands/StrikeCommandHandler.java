package Botlogger.commands;

import Botlogger.model.Strike;
import Botlogger.model.Appeal;
import Botlogger.service.StrikeService;
import Botlogger.service.AppealService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.Permission;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;

public class StrikeCommandHandler extends ListenerAdapter {

    static {
        LoggerFactory.getLogger(StrikeCommandHandler.class);
    }

    private final StrikeService strikeService = new StrikeService();
    private final AppealService appealService = new AppealService();

    private static final String OWNER_USER_ID = "689519709988585648";

    private static final List<String> AUTHORIZED_USER_IDS = Arrays.asList(
            "529480987525251082",
            "852921796679434242"
    );

    private static final List<String> MANAGEMENT_ROLE_IDS = Arrays.asList(
            "898223053832601611",
            "709747039562366977",
            "1329555577646354485",
            "1329555577646354483"
    );

    public List<CommandData> getCommandData() {
        return List.of(
                Commands.slash("strike", "Issue a strike to a user.")
                        .addOption(OptionType.USER, "user", "The user to strike", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the strike", true),
                Commands.slash("strikes", "View strikes of a user.")
                        .addOption(OptionType.USER, "user", "The user to view", true),
                Commands.slash("removestrike", "Remove a specific strike from a user.")
                        .addOption(OptionType.USER, "user", "The user to remove a strike from", true)
                        .addOption(OptionType.INTEGER, "number", "Strike number to remove (1 = first strike)", true),
                Commands.slash("clearstrikes", "Clear all strikes from a user. (Admin Only)")
                        .addOption(OptionType.USER, "user", "The user to clear strikes from", true),
                Commands.slash("editstrike", "Edit the reason of a specific strike. (Admin Only)")
                        .addOption(OptionType.USER, "user", "The user whose strike to edit", true)
                        .addOption(OptionType.INTEGER, "number", "Strike number to edit (1 = first strike)", true)
                        .addOption(OptionType.STRING, "newreason", "New reason for the strike", true),
                Commands.slash("dbinfo", "Show database statistics.")
                        .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("backupstrikes", "Create a backup of the strikes database.")
                        .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

                Commands.slash("appeal", "Appeal one of your strikes.")
                        .addOption(OptionType.INTEGER, "strike", "Strike number to appeal (1 = first strike)", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the appeal", true),
                Commands.slash("myappeals", "View your strike appeals."),
                Commands.slash("pendingappeals", "View all pending appeals. (Staff Only)"),
                Commands.slash("reviewappeal", "Review a pending appeal. (Staff Only)")
                        .addOption(OptionType.INTEGER, "appealid", "Appeal ID to review", true)
                        .addOption(OptionType.STRING, "decision", "approve or deny", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the decision", true),
                Commands.slash("undoappeal", "Reset a user's appeal so they can appeal again. (Admin Only)")
                        .addOption(OptionType.USER, "user", "User whose appeal to reset", true),
                Commands.slash("testtimeout", "Simulate a delayed response to test timeout behavior.")

        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("appeal") || event.getName().equals("myappeals")) {
            switch (event.getName()) {
                case "appeal" -> handleAppeal(event);
                case "myappeals" -> handleMyAppeals(event);
            }
            return;
        }

        if (event.getName().equals("pendingappeals") || event.getName().equals("reviewappeal") || event.getName().equals("undoappeal")) {
            if (!hasManagementPermissions(event)) {
                event.reply("❌ You need management permissions to use this command.").setEphemeral(true).queue();
                return;
            }
            switch (event.getName()) {
                case "pendingappeals" -> handlePendingAppeals(event);
                case "reviewappeal" -> handleReviewAppeal(event);
                case "undoappeal" -> handleUndoAppeal(event);
            }
            return;
        }

        if (!event.getName().equals("strikes") && !hasManagementPermissions(event)) {
            event.reply("❌ You need management permissions or Administrator role to use this command.").setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "strike" -> handleStrike(event);
            case "strikes" -> handleStrikes(event);
            case "removestrike" -> handleRemoveStrike(event);
            case "clearstrikes" -> handleClearStrikes(event);
            case "editstrike" -> handleEditStrike(event);
            case "backupstrikes" -> handleBackupStrikes(event);
            case "dbinfo" -> handleDbInfo(event);
            case "testtimeout" -> handleTestTimeout(event);
        }
    }

    private void handleTestTimeout(SlashCommandInteractionEvent event) {
        // Telling Discord we’ll respond later
        event.deferReply(true).queue();

        // Simulating long DB or API delay
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Safe to reply after deferral
            event.getHook().sendMessage("✅ Delayed reply sent after 5 seconds").queue();
        }).start();
    }

    private boolean hasManagementPermissions(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        String userId = event.getUser().getId();

        if (member == null) return false;

        if (userId.equals(OWNER_USER_ID)) {
            return true;
        }

        if (AUTHORIZED_USER_IDS.contains(userId)) {
            return true;
        }

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        for (String roleId : MANAGEMENT_ROLE_IDS) {
            if (member.getRoles().stream().anyMatch(role -> role.getId().equals(roleId))) {
                return true;
            }
        }

        return false;
    }

    private void handleStrike(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
        Member moderator = event.getMember();


        strikeService.issueStrike(user.getId(), reason, moderator.getId());

        sendStaffStrikeLog(event, user, reason);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⛔ Strike Issued")
                .setColor(Color.CYAN)
                .setDescription(user.getName() + "#" + user.getDiscriminator() + " has been striked.")
                .addField("Reason", reason, false)
                .addField("Now has:", strikeService.getStrikes(user.getId()).size() + " strikes", false)
                .setFooter("Strike System", null)
                .setTimestamp(java.time.Instant.now());

        TextChannel staffStrikesChannel = event.getJDA().getTextChannelById("1372012155964227584");

        event.deferReply(true).queue();

        if (staffStrikesChannel != null) {
            staffStrikesChannel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        event.getHook().sendMessage("✅ Strike issued successfully! Check " + staffStrikesChannel.getAsMention() + " for details.")
                                .setEphemeral(true).queue();
                    },
                    failure -> {
                        event.getHook().sendMessageEmbeds(embed.build()).queue();
                        System.err.println("Failed to send strike to staff channel: " + failure.getMessage());
                    }
            );
        } else {
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            System.err.println("Staff strikes channel not found!");
        }
    }

    private void handleStrikes(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        User user = event.getOption("user").getAsUser();
        List<Strike> strikes = strikeService.getStrikes(user.getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("\uD83D\uDCCB Strike History")
                .setFooter("Strike System", null)
                .setTimestamp(java.time.Instant.now());

        if (strikes.isEmpty()) {
            embed.setColor(Color.CYAN)
                    .setDescription("✅ No strikes found.");
        } else {
            embed.setColor(Color.CYAN);

            for (int i = 0; i < strikes.size(); i++) {
                Strike s = strikes.get(i);

                String displayModerator = s.getModeratorId().matches("\\d+") ? "<@" + s.getModeratorId() + ">" : s.getModeratorId();

                embed.addField("Strike #" + (i + 1),
                        "**Reason:** " + s.getReason() + "\n" +
                                "**Moderator:** " + displayModerator + "\n" +
                                "**Date:** " + s.getDate().substring(0, 10),
                        false);
            }
        }

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleRemoveStrike(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        int strikeNumber = Objects.requireNonNull(event.getOption("number")).getAsInt();

        strikeService.removeStrike(user.getId(), strikeNumber);

        sendStaffStrikeRemovedLog(event, user, strikeNumber);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("✅ Strike Removed")
                .setColor(Color.CYAN)
                .setDescription("Strike #" + strikeNumber + " removed from " + user.getName() + "#" + user.getDiscriminator() + ".")
                .setFooter("Strike System", null)
                .setTimestamp(java.time.Instant.now());

        TextChannel staffStrikesChannel = event.getJDA().getTextChannelById("1372012155964227584");

        if (staffStrikesChannel != null) {
            staffStrikesChannel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        event.reply("✅ Strike removed successfully! Check " + staffStrikesChannel.getAsMention() + " for details.")
                                .setEphemeral(true).queue();
                    },
                    failure -> {
                        event.replyEmbeds(embed.build()).queue();
                        System.err.println("Failed to send strike removal to staff channel: " + failure.getMessage());
                    }
            );
        } else {
            event.replyEmbeds(embed.build()).queue();
            System.err.println("Staff strikes channel not found!");
        }
    }

    private void handleClearStrikes(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        List<Strike> currentStrikes = strikeService.getStrikes(user.getId());

        if (currentStrikes.isEmpty()) {
            event.reply("❌ " + user.getName() + " has no strikes to clear.").setEphemeral(true).queue();
            return;
        }

        int strikeCount = currentStrikes.size();

        strikeService.clearAllStrikes(user.getId());

        sendStaffStrikeClearedLog(event, user, strikeCount);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🧹 All Strikes Cleared")
                .setColor(Color.GREEN)
                .setDescription("All " + strikeCount + " strikes cleared from " + user.getName() + "#" + user.getDiscriminator() + ".")
                .setFooter("Strike System", null)
                .setTimestamp(java.time.Instant.now());

        TextChannel staffStrikesChannel = event.getJDA().getTextChannelById("1372012155964227584");

        if (staffStrikesChannel != null) {
            staffStrikesChannel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        event.reply("✅ All strikes cleared successfully! Check " + staffStrikesChannel.getAsMention() + " for details.")
                                .setEphemeral(true).queue();
                    },
                    failure -> {
                        event.replyEmbeds(embed.build()).queue();
                        System.err.println("Failed to send strike clear to staff channel: " + failure.getMessage());
                    }
            );
        } else {
            event.replyEmbeds(embed.build()).queue();
            System.err.println("Staff strikes channel not found!");
        }
    }

    private void handleEditStrike(SlashCommandInteractionEvent event) {
        User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        int strikeNumber = Objects.requireNonNull(event.getOption("number")).getAsInt();
        String newReason = Objects.requireNonNull(event.getOption("newreason")).getAsString();

        List<Strike> strikes = strikeService.getStrikes(user.getId());

        if (strikes.isEmpty()) {
            event.reply("❌ " + user.getName() + " has no strikes.").setEphemeral(true).queue();
            return;
        }

        if (strikeNumber < 1 || strikeNumber > strikes.size()) {
            event.reply("❌ Invalid strike number. " + user.getName() + " has " + strikes.size() + " strikes.").setEphemeral(true).queue();
            return;
        }

        Strike oldStrike = strikes.get(strikeNumber - 1);
        String oldReason = oldStrike.getReason();

        strikeService.editStrike(user.getId(), strikeNumber, newReason);

        sendStaffStrikeEditedLog(event, user, strikeNumber, oldReason, newReason);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("✏️ Strike Edited")
                .setColor(Color.ORANGE)
                .setDescription("Strike #" + strikeNumber + " edited for " + user.getName() + "#" + user.getDiscriminator() + ".")
                .addField("Old Reason", oldReason, false)
                .addField("New Reason", newReason, false)
                .setFooter("Strike System", null)
                .setTimestamp(java.time.Instant.now());

        TextChannel staffStrikesChannel = event.getJDA().getTextChannelById("1372012155964227584");

        if (staffStrikesChannel != null) {
            staffStrikesChannel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        event.reply("✅ Strike edited successfully! Check " + staffStrikesChannel.getAsMention() + " for details.")
                                .setEphemeral(true).queue();
                    },
                    failure -> {
                        event.replyEmbeds(embed.build()).queue();
                        System.err.println("Failed to send strike edit to staff channel: " + failure.getMessage());
                    }
            );
        } else {
            event.replyEmbeds(embed.build()).queue();
            System.err.println("Staff strikes channel not found!");
        }
    }

    private void handleBackupStrikes(SlashCommandInteractionEvent event) {
        if (!event.getUser().getId().equals(OWNER_USER_ID)) {
            event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
            return;
        }

        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sourceFile = "strikes.db";
        String backupFile = "strikes_backup.db";

        try {
            java.nio.file.Files.copy(
                    java.nio.file.Paths.get(sourceFile),
                    java.nio.file.Paths.get(backupFile),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("\uD83D\uDCBE Strike Database Backup")
                    .setColor(Color.CYAN)
                    .setDescription("Backup created successfully:\n`" + backupFile)
                    .setFooter("Strike System", null)
                    .setTimestamp(java.time.Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } catch (Exception e) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("⚠️ Backup Failed")
                    .setColor(Color.CYAN)
                    .setDescription("An error occurred while creating the backup:\n`" + e.getMessage())
                    .setFooter("Strike System", null)
                    .setTimestamp(java.time.Instant.now());

            event.replyEmbeds(errorEmbed.build()).setEphemeral(true).queue();
        }
    }

    private void sendStaffStrikeLog(SlashCommandInteractionEvent event, User user, String reason) {
        int newStrikeCount = strikeService.getStrikes(user.getId()).size();
        int totalStrikes = 3;
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("⚠️ **STRIKE ISSUED**")
                .setColor(0xFF4444)
                .setDescription("**A strike has been issued to a server member**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                user.getEffectiveName(),
                                user.getId(),
                                user.getId()), true)
                .addField("👮 **Moderator**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📊 **Strike Count**",
                        String.format("**%d/%d**\n%s",
                                newStrikeCount,
                                totalStrikes,
                                getStrikeBar(newStrikeCount, totalStrikes)), true)
                .addField("📝 **Reason**",
                        String.format("```%s```", reason), false)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System • Strike Issued",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        if (newStrikeCount >= totalStrikes) {
            staffLogEmbed.addField("🚨 **CRITICAL WARNING**",
                    "**User has reached maximum strikes! Automatic action may be triggered.**", false);
        } else if (newStrikeCount >= totalStrikes - 1) {
            staffLogEmbed.addField("🚨 **WARNING**",
                    "**User is one strike away from maximum punishment!**", false);
        }

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void handleUndoAppeal(SlashCommandInteractionEvent event) {
        User targetUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
        String targetUserId = targetUser.getId();

        List<Appeal> userAppeals = appealService.getUserAppeals(targetUserId);
        if (userAppeals.isEmpty()) {
            event.reply("❌ " + targetUser.getName() + " has no appeals to undo.").setEphemeral(true).queue();
            return;
        }

        boolean success = appealService.resetUserAppeals(targetUserId);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔄 Appeal Reset")
                    .setColor(0xFFA500)
                    .setDescription(targetUser.getName() + " can now submit an appeal again.")
                    .addField("Appeals Removed", String.valueOf(userAppeals.size()), true)
                    .addField("Reset By", event.getUser().getName(), true)
                    .setFooter("Strike Appeal System", null)
                    .setTimestamp(java.time.Instant.now());

            sendAppealResetLog(event, targetUser, userAppeals.size());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("❌ Failed to reset appeals. Please try again.").setEphemeral(true).queue();
        }
    }

    private void sendAppealResetLog(SlashCommandInteractionEvent event, User targetUser, int appealsRemoved) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("🔄 **APPEAL RESET**")
                .setColor(0xFFA500)
                .setDescription("**A user's appeals have been reset**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                targetUser.getEffectiveName(),
                                targetUser.getId(),
                                targetUser.getId()), true)
                .addField("👮 **Reset By**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("🗑️ **Appeals Removed**",
                        String.format("**%d** appeals deleted", appealsRemoved), true)
                .addField("✅ **Result**",
                        "User can now submit appeals again", false)
                .addField("🕐 **Reset Time**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .setFooter("Strike Appeal System",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendStaffStrikeRemovedLog(SlashCommandInteractionEvent event, User user, int strikeNumber) {
        int remainingStrikes = strikeService.getStrikes(user.getId()).size();
        int totalStrikes = 3;
        String removalReason = "Strike removed by staff";
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("✅ **STRIKE REMOVED**")
                .setColor(0x44FF44)
                .setDescription("**A strike has been removed from a server member**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                user.getEffectiveName(),
                                user.getId(),
                                user.getId()), true)
                .addField("👮 **Moderator**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📊 **Strike Count**",
                        String.format("**%d/%d**\n%s",
                                remainingStrikes,
                                totalStrikes,
                                getStrikeBar(remainingStrikes, totalStrikes)), true)
                .addField("🗑️ **Removed Strike**",
                        String.format("**Strike #%d**", strikeNumber), true)
                .addField("📝 **Removal Reason**",
                        removalReason != null ? String.format("```%s```", removalReason) : "*No reason provided*", false)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System • Strike Removed",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendStaffStrikeClearedLog(SlashCommandInteractionEvent event, User user, int clearedCount) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("🧹 **ALL STRIKES CLEARED**")
                .setColor(0x00FF00)
                .setDescription("**All strikes have been cleared from a server member**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                user.getEffectiveName(),
                                user.getId(),
                                user.getId()), true)
                .addField("👮 **Administrator**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📊 **Strikes Cleared**",
                        String.format("**%d** strikes removed", clearedCount), true)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System • Strikes Cleared",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendStaffStrikeEditedLog(SlashCommandInteractionEvent event, User user, int strikeNumber, String oldReason, String newReason) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("✏️ **STRIKE EDITED**")
                .setColor(0xFFA500)
                .setDescription("**A strike reason has been modified**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                user.getEffectiveName(),
                                user.getId(),
                                user.getId()), true)
                .addField("👮 **Administrator**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📝 **Strike Number**",
                        String.format("**Strike #%d**", strikeNumber), true)
                .addField("📝 **Old Reason**",
                        String.format("```%s```", oldReason), false)
                .addField("📝 **New Reason**",
                        String.format("```%s```", newReason), false)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System • Strike Edited",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendStaffStrikeHistoryLog(SlashCommandInteractionEvent event, User user, List<Strike> strikes) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("📊 **STRIKE HISTORY VIEWED**")
                .setColor(0x4444FF)
                .setDescription("**Strike history was accessed for a server member**")
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                user.getEffectiveName(),
                                user.getId(),
                                user.getId()), true)
                .addField("👮 **Accessed By**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📈 **Total Strikes**",
                        String.format("**%d** strikes on record", strikes.size()), true)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System • History Accessed",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendLogMessage(SlashCommandInteractionEvent event, MessageEmbed embed) {
        TextChannel logChannel = event.getJDA().getTextChannelById("1371968158981816401");
        if (logChannel != null) {
            logChannel.sendMessage("").setEmbeds(embed).queue(
                    success -> {

                    },
                    failure -> {

                        System.err.println("Failed to send staff log: " + failure.getMessage());
                    }
            );
        } else {
            System.err.println("Staff log channel not found!");
        }
    }

    private String getStrikeBar(int current, int total) {
        int filled = Math.min(current, total);
        int empty = total - filled;

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            bar.append("🔴");
        }
        for (int i = 0; i < empty; i++) {
            bar.append("⚪");
        }
        return bar.toString();
    }

    private String generateActionId() {
        return "MOD-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    private int getCurrentStrikeCount(User user) {
        return 0;
    }

    private int getMaxStrikes() {
        return 5;
    }

    private void sendStaffActionLog(SlashCommandInteractionEvent event, String action, User targetUser, String details, int color) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("🛡️ **STAFF ACTION**")
                .setColor(color)
                .setDescription(String.format("**%s**", action))
                .addField("📋 **Target User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                targetUser.getEffectiveName(),
                                targetUser.getId(),
                                targetUser.getId()), true)
                .addField("👮 **Staff Member**",
                        String.format("**%s**\n<@%s>",
                                staffName,
                                event.getUser().getId()), true)
                .addField("📝 **Details**",
                        String.format("```%s```", details), false)
                .addField("🕐 **Timestamp**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action ID**",
                        String.format("`%s`", generateActionId()), true)
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .setFooter("Server Moderation System",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void handleDbInfo(SlashCommandInteractionEvent event) {
        if (!event.getUser().getId().equals(OWNER_USER_ID)) {
            event.reply("❌ You are not authorized to use this command.").setEphemeral(true).queue();
            return;
        }

        List<String> usersWithStrikes = getAllUsersWithStrikes();

        StringBuilder info = new StringBuilder();
        info.append("**All Users With Strikes:**\n\n");

        int totalStrikes = 0;
        int totalUsers = 0;

        for (String userId : usersWithStrikes) {
            List<Strike> strikes = strikeService.getStrikes(userId);
            if (!strikes.isEmpty()) {
                totalUsers++;
                totalStrikes += strikes.size();

                String displayName = userId;
                try {
                    User user = event.getJDA().getUserById(userId);
                    if (user != null) {
                        displayName = user.getName();
                    }
                } catch (Exception ignored) {

                }

                info.append(String.format("**%s** (<@%s>): **%d** strikes\n",
                        displayName, userId, strikes.size()));
            }
        }

        info.append(String.format("\n**Summary:**\n"));
        info.append(String.format("• Total users with strikes: **%d**\n", totalUsers));
        info.append(String.format("• Total strikes issued: **%d**\n", totalStrikes));

        if (totalStrikes > 0) {
            info.append("\n✅ **Database has data!**");
        } else {
            info.append("\n❌ **Database appears empty!**");
        }

        String responseStr = info.toString();
        if (responseStr.length() > 4096) {

            String firstPart = responseStr.substring(0, 4000) + "...\n\n*Response truncated due to length*";
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📊 Database Information (Part 1)")
                    .setColor(Color.CYAN)
                    .setDescription(firstPart)
                    .setFooter("Strike System", null)
                    .setTimestamp(java.time.Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📊 Complete Database Information")
                    .setColor(Color.CYAN)
                    .setDescription(responseStr)
                    .setFooter("Strike System", null)
                    .setTimestamp(java.time.Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    private List<String> getAllUsersWithStrikes() {
        return strikeService.getAllUsersWithStrikes();
    }

    private void handleAppeal(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        int strikeNumber = Objects.requireNonNull(event.getOption("strike")).getAsInt();
        String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();

        if (appealService.hasEverSubmittedAppeal(userId)) {
            event.reply("❌ You have already used your one appeal. Each user can only appeal once.").setEphemeral(true).queue();
            return;
        }

        List<Strike> strikes = strikeService.getStrikes(userId);

        if (strikes.isEmpty()) {
            event.reply("❌ You don't have any strikes to appeal.").setEphemeral(true).queue();
            return;
        }

        if (strikeNumber < 1 || strikeNumber > strikes.size()) {
            event.reply("❌ Invalid strike number. You have " + strikes.size() + " strikes.").setEphemeral(true).queue();
            return;
        }

        boolean success = appealService.createAppeal(userId, strikeNumber, reason);

        if (success) {
            Strike appealedStrike = strikes.get(strikeNumber - 1);

            List<Appeal> userAppeals = appealService.getUserAppeals(userId);
            int appealId = userAppeals.get(0).getId();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📩 Appeal Submitted")
                    .setColor(Color.BLUE)
                    .setDescription("Your appeal has been submitted and is pending review.\n⚠️ **This was your only appeal - choose wisely!**")
                    .addField("Appeal ID", String.valueOf(appealId), true)
                    .addField("Strike #" + strikeNumber, appealedStrike.getReason(), false)
                    .addField("Appeal Reason", reason, false)
                    .setFooter("Strike Appeal System", null)
                    .setTimestamp(java.time.Instant.now());

            sendAppealSubmittedLog(event, appealedStrike, reason, appealId);

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("❌ Failed to submit appeal. You may have already used your one appeal.").setEphemeral(true).queue();
        }
    }

    private void handleMyAppeals(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        List<Appeal> appeals = appealService.getUserAppeals(userId);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Your Appeals")
                .setColor(Color.BLUE)
                .setFooter("Strike Appeal System", null)
                .setTimestamp(java.time.Instant.now());

        if (appeals.isEmpty()) {
            embed.setDescription("✅ You haven't submitted any appeals.");
        } else {
            StringBuilder description = new StringBuilder();

            for (Appeal appeal : appeals) {
                Strike strike = appealService.getStrikeById(appeal.getStrikeId());
                String strikeReason = strike != null ? strike.getReason() : "Unknown";

                String statusEmoji = switch (appeal.getStatus()) {
                    case "PENDING" -> "🟡";
                    case "APPROVED" -> "✅";
                    case "DENIED" -> "❌";
                    default -> "⚪";
                };

                description.append(String.format("**Appeal #%d** %s %s\n",
                        appeal.getId(), statusEmoji, appeal.getStatus()));
                description.append(String.format("**Strike:** %s\n", strikeReason));
                description.append(String.format("**Your Reason:** %s\n", appeal.getReason()));

                if (!appeal.getStatus().equals("PENDING") && appeal.getReviewReason() != null) {
                    description.append(String.format("**Staff Response:** %s\n", appeal.getReviewReason()));
                }
                description.append("\n");
            }

            embed.setDescription(description.toString());
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void handlePendingAppeals(SlashCommandInteractionEvent event) {
        List<Appeal> pendingAppeals = appealService.getAllPendingAppeals();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⏳ Pending Appeals")
                .setColor(Color.ORANGE)
                .setFooter("Strike Appeal System", null)
                .setTimestamp(java.time.Instant.now());

        if (pendingAppeals.isEmpty()) {
            embed.setDescription("✅ No pending appeals to review.");
        } else {
            StringBuilder description = new StringBuilder();
            description.append("**Appeals awaiting review:**\n\n");

            for (Appeal appeal : pendingAppeals) {
                Strike strike = appealService.getStrikeById(appeal.getStrikeId());
                String strikeReason = strike != null ? strike.getReason() : "Unknown";

                description.append(String.format("**Appeal ID:** %d\n", appeal.getId()));
                description.append(String.format("**User:** <@%s>\n", appeal.getUserId()));
                description.append(String.format("**Strike:** %s\n", strikeReason));
                description.append(String.format("**Appeal Reason:** %s\n", appeal.getReason()));
                description.append(String.format("**Submitted:** %s\n",
                        appeal.getDate().substring(0, 16)));
                description.append("─────────────────────\n");
            }

            embed.setDescription(description.toString());
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void handleReviewAppeal(SlashCommandInteractionEvent event) {
        int appealId = Objects.requireNonNull(event.getOption("appealid")).getAsInt();
        String decision = Objects.requireNonNull(event.getOption("decision")).getAsString().toLowerCase();
        String reviewReason = Objects.requireNonNull(event.getOption("reason")).getAsString();

        if (!decision.equals("approve") && !decision.equals("deny")) {
            event.reply("❌ Decision must be either 'approve' or 'deny'.").setEphemeral(true).queue();
            return;
        }

        Appeal appeal = appealService.getAppealById(appealId);
        if (appeal == null) {
            event.reply("❌ Appeal not found.").setEphemeral(true).queue();
            return;
        }

        if (!appeal.getStatus().equals("PENDING")) {
            event.reply("❌ This appeal has already been reviewed.").setEphemeral(true).queue();
            return;
        }

        String reviewerId = event.getUser().getId();
        boolean success;

        if (decision.equals("approve")) {
            success = appealService.approveAppeal(appealId, reviewerId, reviewReason);
        } else {
            success = appealService.denyAppeal(appealId, reviewerId, reviewReason);
        }

        if (success) {
            Strike strike = appealService.getStrikeById(appeal.getStrikeId());
            String strikeReason = strike != null ? strike.getReason() : "Unknown";

            int embedColor = decision.equals("approve") ? 0x00FF00 : 0xFF0000;
            String title = decision.equals("approve") ? "✅ Appeal Approved" : "❌ Appeal Denied";

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title)
                    .setColor(embedColor)
                    .addField("Appeal ID", String.valueOf(appealId), true)
                    .addField("User", "<@" + appeal.getUserId() + ">", true)
                    .addField("Strike", strikeReason, false)
                    .addField("User's Reason", appeal.getReason(), false)
                    .addField("Your Decision", reviewReason, false)
                    .setFooter("Strike Appeal System", null)
                    .setTimestamp(java.time.Instant.now());

            sendAppealReviewedLog(event, appeal, strike, decision, reviewReason);

            TextChannel staffStrikesChannel = event.getJDA().getTextChannelById("1372012155964227584");
            if (staffStrikesChannel != null) {
                staffStrikesChannel.sendMessageEmbeds(embed.build()).queue();
            }

            event.reply("✅ Appeal " + decision + "d successfully!").setEphemeral(true).queue();
        } else {
            event.reply("❌ Failed to " + decision + " appeal. Please try again.").setEphemeral(true).queue();
        }
    }

    private void sendAppealSubmittedLog(SlashCommandInteractionEvent event, Strike strike, String appealReason, int appealId) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown User";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle("📩 **APPEAL SUBMITTED**")
                .setColor(0x4169E1)
                .setDescription("**A user has submitted a strike appeal**")
                .addField("📋 **User**",
                        String.format("**%s**\n`%s`\n<@%s>",
                                event.getUser().getEffectiveName(),
                                event.getUser().getId(),
                                event.getUser().getId()), true)
                .addField("🆔 **Appeal ID**",
                        String.format("`%d`", appealId), true)
                .addField("📝 **Original Strike**",
                        String.format("```%s```", strike.getReason()), false)
                .addField("💬 **Appeal Reason**",
                        String.format("```%s```", appealReason), false)
                .addField("🕐 **Submitted**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("⚡ **Action Required**",
                        String.format("Use `/reviewappeal %d approve/deny reason`", appealId), true)
                .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                .setFooter("Strike Appeal System",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }

    private void sendAppealReviewedLog(SlashCommandInteractionEvent event, Appeal appeal, Strike strike, String decision, String reviewReason) {
        Member staffMember = event.getMember();
        String staffName = staffMember != null ? staffMember.getEffectiveName() : "Unknown Staff";

        int logColor = decision.equals("approve") ? 0x00FF00 : 0xFF0000;
        String title = decision.equals("approve") ? "✅ **APPEAL APPROVED**" : "❌ **APPEAL DENIED**";
        String description = decision.equals("approve") ?
                "**An appeal has been approved - strike removed**" :
                "**An appeal has been denied**";

        EmbedBuilder staffLogEmbed = new EmbedBuilder()
                .setTitle(title)
                .setColor(logColor)
                .setDescription(description)
                .addField("📋 **User**",
                        String.format("**User ID: %s**\n<@%s>", appeal.getUserId(), appeal.getUserId()), true)
                .addField("👮 **Reviewed By**",
                        String.format("**%s**\n<@%s>", staffName, event.getUser().getId()), true)
                .addField("📝 **Original Strike**",
                        String.format("```%s```", strike != null ? strike.getReason() : "Unknown"), false)
                .addField("💬 **User's Appeal**",
                        String.format("```%s```", appeal.getReason()), false)
                .addField("⚖️ **Staff Decision**",
                        String.format("```%s```", reviewReason), false)
                .addField("🕐 **Decision Time**",
                        String.format("<t:%d:F>", System.currentTimeMillis() / 1000), true)
                .addField("🆔 **Appeal ID**",
                        String.format("`%d`", appeal.getId()), true)
                .setFooter("Strike Appeal System",
                        event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(java.time.Instant.now());

        sendLogMessage(event, staffLogEmbed.build());
    }
}