package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.mojang.blaze3d.vertex.MeshData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.LittleTiles;
import team.creative.creativecore.client.render.VertexFormatUtils;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.mixin.client.render.ByteBufferBuilderResultAccessor;
import team.creative.littletiles.mixin.client.render.MeshDataAccessor;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public class BufferHolder implements BufferCache {

    public static BufferHolder combine(BufferHolder first, BufferHolder second) { return null; }

    private ByteBuffer buffer;
    private int length;
    private int vertexCount;
    private int[] indexes;
    private boolean invalid;
    private int uploadIndex;
    public BufferHolder(ByteBuffer buffer, int length, int vertexCount, int[] indexes) {}

    public BufferHolder(MeshData buffer, int[] indexes) {}

    public void eraseBuffer() {}

    public boolean upload(ChunkBufferUploader uploader) { return false; }

    public boolean upload(int facing, ChunkBufferUploader uploader) { return false; }

    public int[] indexes() { return null; }

    public ByteBuffer byteBuffer() { return null; }

    public ByteBuffer byteBufferOrThrow() { return null; }

    public int length() { return 0; }

    public int lengthToUpload() { return 0; }

    public int lengthToUpload(int facing) { return 0; }

    public int vertexCount() { return 0; }

    public boolean isEmpty() { return false; }

    public boolean isInvalid() { return false; }

    public void invalidate() {}

    public boolean isAvailable() { return false; }

    public boolean download(ChunkBufferDownloader downloader) { return false; }

    public boolean download(ByteBuffer buffer) { return false; }

    private int indexOf(IntList list, int index) { return 0; }

    private void add(IntList list, int index, int length) {}

    public BufferCache combine(Iterator<BufferCache> itr) { return null; }

    public BufferHolder copy() { return null; }

    private boolean isIdentitcal(int[] toExtract) { return false; }

    public BufferHolder extract(int toExtract) { return null; }

    public BufferHolder extract(int[] toExtract) { return null; }

    public void applyOffset(Vec3 vec, int sectionIndex) {}

    public String toString() { return null; }

}

