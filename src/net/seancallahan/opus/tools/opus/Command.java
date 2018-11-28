package net.seancallahan.opus.tools.opus;

public interface Command
{
    void run(String[] args);

    String getDescription();
}
