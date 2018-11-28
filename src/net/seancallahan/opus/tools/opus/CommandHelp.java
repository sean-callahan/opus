package net.seancallahan.opus.tools.opus;

import java.util.ArrayList;
import java.util.Collections;

public class CommandHelp implements Command
{
    @Override
    public void run(String[] args)
    {
        System.out.println("Opus command");
        System.out.println("Usage: opus <command> [arguments]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println();

        ArrayList<String> names = new ArrayList(Opus.commands.keySet());
        Collections.sort(names);

        for (String name : names)
        {
            Command cmd = Opus.commands.get(name);
            System.out.printf("\t%-10s%s\n", name, cmd.getDescription());
        }
    }

    @Override
    public String getDescription()
    {
        return "shows this page";
    }
}
