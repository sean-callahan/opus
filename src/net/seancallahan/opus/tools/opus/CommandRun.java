package net.seancallahan.opus.tools.opus;

public class CommandRun implements Command
{
    @Override
    public void run(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("must supply an OAR file");
            System.exit(1);
        }
    }

    @Override
    public String getDescription()
    {
        return "runs an OAR file";
    }
}
