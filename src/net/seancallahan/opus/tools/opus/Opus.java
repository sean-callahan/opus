package net.seancallahan.opus.tools.opus;

import java.util.Arrays;
import java.util.Map;

public class Opus
{
    public static final String VERSION = "1.0.0";

    protected static final Map<String, Command> commands;

    public static void main(String[] args)
    {
        String commandName;
        if (args.length == 0)
        {
            commandName = "help";
        }
        else
        {
            commandName = args[0].trim().toLowerCase();
        }

        if (!commands.containsKey(commandName))
        {
            System.out.println("Unknown command");
            System.exit(1);
        }

        Command command = commands.get(commandName);
        command.run(Arrays.copyOfRange(args, 1, args.length));
    }

    static
    {
        commands = Map.of(
            "build", new CommandBuild(),
            "help", new CommandHelp(),
            "version", new CommandVersion()
        );
    }
}
