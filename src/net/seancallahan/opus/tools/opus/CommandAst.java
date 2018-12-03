package net.seancallahan.opus.tools.opus;

import net.seancallahan.opus.compiler.Compiler;
import net.seancallahan.opus.compiler.SourceFile;
import net.seancallahan.opus.lang.Declaration;
import net.seancallahan.opus.lang.Method;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CommandAst implements Command
{
    @Override
    public void run(String[] args)
    {
        for (String arg : args)
        {
            Compiler compiler = new Compiler();

            try
            {
                File file = new File(arg);
                SourceFile source = compiler.compile(file, false);

                File astFile = new File(file.getParent() + File.pathSeparator + file.getName() + ".ast");
                astFile.delete();
                astFile.createNewFile();

                FileOutputStream fileOut = new FileOutputStream(astFile);
                DataOutputStream out = new DataOutputStream(fileOut);

                for (Declaration declaration : source.getParser().getDeclarations().values())
                {
                    if (declaration instanceof Method)
                    {
                        Method method = (Method)declaration;
                        method.getBody().writeTo(out);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "generates an .ast file";
    }
}
