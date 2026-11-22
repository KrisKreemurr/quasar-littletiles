package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.systems.RenderSystem;

// QUASAR STUB: generated no-op stand-in; the original lives in src/parked (render rewrite pending).

public interface VertexBufferExtender {




    public int getIndexCount();

    public void setIndexCount(int count);


    public RenderSystem.AutoStorageIndexBuffer getSequentialIndices();

    public void setSequentialIndices(RenderSystem.AutoStorageIndexBuffer indexBuffer);

    public void setLastUploadedLength(int length);

    public int getLastUploadedLength();

    public int getVertexBufferId();

}

