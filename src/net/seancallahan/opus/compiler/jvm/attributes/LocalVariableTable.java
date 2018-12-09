package net.seancallahan.opus.compiler.jvm.attributes;

import net.seancallahan.opus.compiler.jvm.Constant;
import net.seancallahan.opus.compiler.jvm.ConstantPool;
import net.seancallahan.opus.compiler.jvm.Descriptor;
import net.seancallahan.opus.lang.Variable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalVariableTable extends Attribute
{
    private final List<LocalVariable> items = new ArrayList<>();

    public LocalVariableTable(ConstantPool pool)
    {
        super(pool, "LocalVariableTable");
    }

    public void add(Variable variable, short codePosition, short codeLength, short index)
    {
        items.add(new LocalVariable(pool, variable, codePosition, codeLength, index));
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        buffer.writeShort(items.size());
        for (LocalVariable item : items)
        {
            item.write(buffer);
        }

        super.write(out);
    }

    private class LocalVariable
    {
        private final short startPc;
        private final short length;
        private final short nameIndex;
        private final short descriptorIndex;
        private final short index;

        private LocalVariable(ConstantPool pool, Variable variable, short position, short length, short index)
        {
            this.startPc = (short)(position - length);
            this.length = length;
            this.nameIndex = pool.add(new Constant.UTF8(pool, variable.getName().getValue()));
            this.descriptorIndex = pool.add(new Constant.UTF8(pool, Descriptor.from(variable).toString()));
            this.index = index;
        }

        private void write(DataOutputStream out) throws IOException
        {
            out.writeShort(startPc);
            out.writeShort(length);
            out.writeShort(nameIndex);
            out.writeShort(descriptorIndex);
            out.writeShort(index);
        }
    }
}