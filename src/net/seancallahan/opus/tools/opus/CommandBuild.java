package net.seancallahan.opus.tools.opus;

import net.seancallahan.opus.compiler.Compiler;

public class CommandBuild implements Command
{
    @Override
    public void run(String[] args)
    {
        for (String arg : args)
        {
            System.out.printf("build %s ", arg);

            long start = System.nanoTime();

            try
            {
                Compiler compiler = new Compiler();
                compiler.compile(new java.io.File(arg));
            }
            catch (Exception e)
            {
                System.out.print("failed\n");
                e.printStackTrace();
                System.exit(1);
            }

            long duration = System.nanoTime() - start;
            double ms = duration / 1e6;

            System.out.printf("success %.1fms\n", ms);
        }
    }

    @Override
    public String getDescription()
    {
        return "builds source files";
    }
}
