package com.yungnickyoung.minecraft.bettercaves.util;

import cn.tesseract.mycelium.util.BlockPos;

public class MutableBlockPos extends BlockPos {

    public MutableBlockPos(BlockPos pos) {
        super(pos.x, pos.y, pos.z);
    }

    public MutableBlockPos() {
        super();
    }

    public MutableBlockPos move(EnumFacing facing, int n) {
        this.set(this.x + facing.getXOffset() * n, this.y + facing.getYOffset() * n, this.z + facing.getZOffset() * n);
        return this;
    }
}
