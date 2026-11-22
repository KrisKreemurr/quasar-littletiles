# Parked LittleTiles render code (Quasar 26.3 port)

These are the original 1.21.1 LittleTiles render classes and render mixins. They target the old
OpenGL chunk pipeline (RenderType/VertexBuffer/CompiledSection/BufferUploader…) that Minecraft 26.x removed.

They are **not compiled**. The build uses small no-op stubs in `src/main/java` with the same names so the rest
of LittleTiles compiles and boots (tiles exist, load and save, but are not drawn yet).

The render rewrite (into the chunk-mesh path Quasium uses) moves code back from here piece by piece.
