
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Fembot extends ListenerAdapter{

    public static void main(String[] args) throws LoginException{
        String data = "noFile";
        try {
            File myObj = new File("../tokens");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) { data = myReader.nextLine();} myReader.close();
        } catch (FileNotFoundException e) {}
        if (data == "noFile"){ System.out.println("Failed to load tokens file"); System.exit(0);}

        JDA jda = JDABuilder.createLight(data, EnumSet.noneOf(GatewayIntent.class)).addEventListeners(new Fembot()).build();

        // These commands take up to an hour to be activated after creation/update/delete
        CommandListUpdateAction commands = jda.updateCommands();

        // Simple reply commands
        commands.addCommands(
            Commands.slash("say", "Makes the bot say what you tell it to")
            .addOption(STRING, "content", "What the bot should say", true) /* you can add required options like this too*/ );
        commands.addCommands(
            Commands.slash("prune", "Prune messages from this channel")
            .addOption(INTEGER, "amount", "How many messages to prune (Default 100)") /*simple optional argument*/ );
        commands.addCommands(
            Commands.slash("howgay", "Sends percentage of your gayness")
            .addOption(USER, "user", "The specific user to check (Default Yourself)"));

        commands.queue();       // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        // Only accept commands from guilds
        if (event.getGuild() == null) return;
        
        User usr=null;

        switch (event.getName()){
            case "say": event.reply(event.getOption("content").getAsString()).queue(); break;
            case "prune": prune(event); break;
            case "howgay": 
                if (event.getOption("user") == null) usr=event.getUser();
                else usr=event.getOption("user").getAsUser(); 
                int per = (int)(Math.random() * (100 - 0 + 1) + 0);
                event.reply(usr.getAsMention() + " is " + per + "% gay").queue();
                break;
            default: event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String[] id = event.getComponentId().split(":"); // this is the custom id we specified in our button
        String authorId = id[0];
        String type = id[1];
        // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
        if (!authorId.equals(event.getUser().getId())) return;

        event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail
 
        MessageChannel channel = event.getChannel();
        switch (type){
            case "prune":
                int amount = Integer.parseInt(id[2]);
                event.getChannel().getIterableHistory()
                    .skipTo(event.getMessageIdLong())
                    .takeAsync(amount)
                    .thenAccept(channel::purgeMessages);
                // fallthrough delete the prompt message with our buttons
            case "delete":
                event.getHook().deleteOriginal().queue();
        }
    }


    public void prune(SlashCommandInteractionEvent event)
    {
        OptionMapping amountOption = event.getOption("amount"); // This is configured to be optional so check for null
        int amount = amountOption == null
                ? 100 // default 100
                : (int) Math.min(200, Math.max(2, amountOption.getAsLong())); // enforcement: must be between 2-200
        String userId = event.getUser().getId();
        event.reply("This will delete " + amount + " messages.\nAre you sure?") // prompt the user with a button menu
            .addActionRow(// this means "<style>(<id>, <label>)", you can encode anything you want in the id (up to 100 characters)
                Button.secondary(userId + ":delete", "Nevermind!"),
                Button.danger(userId + ":prune:" + amount, "Yes!")) // the first parameter is the component id we use in onButtonInteraction above
            .queue();
    }
}