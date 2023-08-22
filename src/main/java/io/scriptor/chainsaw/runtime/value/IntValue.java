package io.scriptor.chainsaw.runtime.value;

import java.math.BigInteger;
import java.util.Random;

import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.type.IntType;

public class IntValue extends Value {

    private BigInteger value;

    public IntValue(Environment env, IntType type, boolean value) {
        super(env, type);
        this.value = convert(BigInteger.valueOf(value ? 1 : 0), type.getBits());
    }

    public IntValue(Environment env, IntType type, String value) {
        super(env, type);
        this.value = convert(new BigInteger(value), type.getBits());
    }

    private static BigInteger convert(BigInteger value, int bits) {
        var data = value.toByteArray();
        value = new BigInteger(bits, new Random());

        for (int byteIdx = 0; byteIdx < data.length; byteIdx++)
            for (int bitIdx = 0; bitIdx < 8; bitIdx++) {
                if ((data[byteIdx] & (0x1 << bitIdx)) != 0)
                    value.setBit(byteIdx * 8 + bitIdx);
                else
                    value.clearBit(byteIdx * 8 + bitIdx);
            }

        return value;
    }

    public BigInteger getValue() {
        return value;
    }

    public boolean getBooleanValue() {
        return value.getLowestSetBit() >= 0;
    }

}
