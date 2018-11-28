package net.seancallahan.opus.tools.opus;

public class CommandVersion implements Command
{
    @Override
    public void run(String[] args)
    {
        System.out.println(String.format("Opus version %s", Opus.VERSION));
    }

    @Override
    public String getDescription()
    {
        return "prints the version";
    }
}
