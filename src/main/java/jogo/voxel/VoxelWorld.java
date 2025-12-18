package jogo.voxel;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import jogo.util.Hit;
import jogo.util.ProcTextures;
import jogo.util.furnace.FurnaceState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VoxelWorld {
    private final AssetManager assetManager;
    private final int sizeX, sizeY, sizeZ;
    private final VoxelPalette palette;
    private final int baseHeight = 20;
    private final int amplitude = 10;
    private final float frequency = 0.05f;
    private static final byte AIR = VoxelPalette.AIR_ID;
    private static final byte GRASS = VoxelPalette.GRASS_ID;
    private static final byte WOOD = VoxelPalette.WOOD_ID;
    private static final byte LEAVES = VoxelPalette.LEAVES_ID;
    private static final byte STONE = VoxelPalette.STONE_ID; // 1
    private static final byte DIRT = VoxelPalette.DIRT_ID; // 2
    private static final byte SAND = VoxelPalette.SAND_ID; // 3
    private static final byte METALORE = VoxelPalette.METALORE_ID; // 4
    private static final byte SOULSAND = VoxelPalette.SOULSAND_ID; // 6
    private static final byte HOTBLOCK = VoxelPalette.HOTBLOCK_ID; // 8
    private static final byte BEDROCK =  VoxelPalette.BEDROCK_ID; // 12

    private final Node node = new Node("VoxelWorld");
    private final Map<Byte, Geometry> geoms = new HashMap<>();
    private final Map<Byte, Material> materials = new HashMap<>();

    private boolean lit = true;       // Shading: On by default
    private boolean wireframe = false; // Wireframe: Off by default
    private boolean culling = true;   // Culling: On by default
    private int groundHeight = 8; // baseline Y level

    private static final int TICKET_RADIUS = 8;

    private final Map<Vector3i, FurnaceState> furnaceStates = new HashMap<>();

    // Chunked world data
    private final int chunkSize = Chunk.SIZE;
    private final int chunkCountX, chunkCountY, chunkCountZ;
    private final Chunk[][][] chunks;

    public VoxelWorld(AssetManager assetManager, int sizeX, int sizeY, int sizeZ) {
        this.assetManager = assetManager;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palette = VoxelPalette.defaultPalette();
        // Remove old vox array
        // this.vox = new byte[sizeX][sizeY][sizeZ];
        this.chunkCountX = (int)Math.ceil(sizeX / (float)chunkSize);
        this.chunkCountY = (int)Math.ceil(sizeY / (float)chunkSize);
        this.chunkCountZ = (int)Math.ceil(sizeZ / (float)chunkSize);
        this.chunks = new Chunk[chunkCountX][chunkCountY][chunkCountZ];
        for (int cx = 0; cx < chunkCountX; cx++)
            for (int cy = 0; cy < chunkCountY; cy++)
                for (int cz = 0; cz < chunkCountZ; cz++)
                    chunks[cx][cy][cz] = new Chunk(cx, cy, cz);
        initMaterials();
    }

    // Helper to get chunk and local coordinates
    private Chunk getChunk(int x, int y, int z) {
        int cx = x / chunkSize;
        int cy = y / chunkSize;
        int cz = z / chunkSize;
        if (cx < 0 || cy < 0 || cz < 0 || cx >= chunkCountX || cy >= chunkCountY || cz >= chunkCountZ) return null;
        return chunks[cx][cy][cz];
    }
    private int lx(int x) { return x % chunkSize; }
    private int ly(int y) { return y % chunkSize; }
    private int lz(int z) { return z % chunkSize; }

    // Block access
    public byte getBlock(int x, int y, int z) {
        Chunk c = getChunk(x, y, z);
        if (c == null) return VoxelPalette.AIR_ID;
        if (!inBounds(x,y,z)) return VoxelPalette.AIR_ID;
        return c.get(lx(x), ly(y), lz(z));
    }
    public void setBlock(int x, int y, int z, byte id) {
        Chunk c = getChunk(x, y, z);
        if (c != null) {
            c.set(lx(x), ly(y), lz(z), id);
            c.markDirty();
            // If on chunk edge, mark neighbor dirty
            if (lx(x) == 0) markNeighborChunkDirty(x-1, y, z);
            if (lx(x) == chunkSize-1) markNeighborChunkDirty(x+1, y, z);
            if (ly(y) == 0) markNeighborChunkDirty(x, y-1, z);
            if (ly(y) == chunkSize-1) markNeighborChunkDirty(x, y+1, z);
            if (lz(z) == 0) markNeighborChunkDirty(x, y, z-1);
            if (lz(z) == chunkSize-1) markNeighborChunkDirty(x, y, z+1);
        }
    }

    private void markNeighborChunkDirty(int x, int y, int z) {
        Chunk n = getChunk(x, y, z);
        if (n != null) n.markDirty();
    }

    public boolean breakAt(int x, int y, int z) {
        if (!inBounds(x,y,z)) return false;

        if (getBlock(x, y, z) == VoxelPalette.FURNACE_ID) {
            removeFurnaceState(x, y, z); // Limpa o estado persistente
        }

        setBlock(x, y, z, VoxelPalette.AIR_ID);
        return true;
    }

    public Node getNode() { return node; }



    //TODO this is where you'll generate your world
    public void generateLayers() {

        SimpleNoise heightNoise = new SimpleNoise(2742);
        SimpleNoise biomeNoise = new SimpleNoise(1268);

        int width = sizeX;
        int depth = sizeZ;

        int baseHeight = 20;
        int amplitude = 8;
        float biomeFrequency = 0.005f; // Frequência baixa para grandes biomas

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {

                // Determinar o Bioma
                float biomeN = biomeNoise.noise(x * biomeFrequency, z * biomeFrequency);

                byte topBlock;
                float oreChance;
                int surfaceDepth;
                boolean isHot = false;
                boolean isDesert = false;

                if (biomeN < -0.3f) {
                    topBlock = SAND;
                    oreChance = 0.0f;
                    surfaceDepth = 5;
                    isDesert = true;

                } else if (biomeN > 0.3f) {
                    topBlock = HOTBLOCK;
                    oreChance = 0.2f;
                    surfaceDepth = 3;
                    isHot = true;

                } else { // Bioma Padrão (Grass)
                    topBlock = GRASS;
                    oreChance = 0.015f;
                    surfaceDepth = 3;
                }

                // Calcular Altura
                float n = heightNoise.noise(x * 0.02f, z * 0.02f);
                int height = baseHeight + (int) (n * amplitude);

                // Limites verticais
                if (height < 3) height = 3;
                if (height >= sizeY - 1) height = sizeY - 2;

                // Colocar Blocos

                for (int y = height; y >= 0; y--) {

                    if (y > height) continue;

                    if (isHot) {
                        if (y >= height - 2) {
                            setBlock(x, y, z, HOTBLOCK);
                        } else {
                            if (Math.random() < oreChance) {
                                setBlock(x, y, z, METALORE);
                            } else {
                                setBlock(x, y, z, STONE);
                            }
                        }

                    } else if (isDesert) {
                        if (y > height - surfaceDepth) { // Camadas de Areia
                            if (Math.random() < 0.05 && y >= height - 2) {
                                setBlock(x, y, z, SOULSAND);
                            } else {
                                setBlock(x, y, z, SAND);
                            }
                        } else {
                            setBlock(x, y, z, BEDROCK);
                        }

                    } else { // Bioma Padrão
                        if (y == height) {
                            setBlock(x, y, z, GRASS);
                        } else if (y > height - surfaceDepth) {
                            setBlock(x, y, z, DIRT);
                        } else {
                            if (y < baseHeight - 4 && Math.random() < oreChance) {
                                setBlock(x, y, z, METALORE);
                            } else {
                                setBlock(x, y, z, STONE);
                            }
                        }
                    }
                }

                // 4. Geração de Árvores
                if (getBlock(x, height + 1, z) == AIR) {
                    if (!isDesert && !isHot && Math.random() < 0.005) {
                        generateTree(x, height + 1, z);
                    }
                    if (isHot && Math.random() < 0.005) {
                        generateHotTree(x, height + 1, z);
                    }
                }
            }

        }
    }

    private int getHeightAt(int x, int z, SimpleNoise noise) {
        float n = noise.noise((int) (x * frequency), (int) (z * frequency));
        return baseHeight + (int) (n * amplitude);
    }

    public int getTopSolidY(int x, int z) {
        if (x < 0 || z < 0 || x >= sizeX || z >= sizeZ) return -1;
        for (int y = sizeY - 1; y >= 0; y--) {
            if (palette.get(getBlock(x, y, z)).isSolid()) return y;
        }
        return -1;
    }

    public Vector3f getRecommendedSpawn() {
        int cx = sizeX / 2;
        int cz = sizeZ / 2;
        int ty = getTopSolidY(cx, cz);
        if (ty < 0) ty = groundHeight;
        return new Vector3f(cx + 0.5f, ty + 3.0f, cz + 0.5f);
    }

    private void initMaterials() {
        // Single material for STONE blocks
        Texture2D tex = ProcTextures.checker(128, 4, ColorRGBA.Gray, ColorRGBA.DarkGray);
        materials.put(VoxelPalette.STONE_ID, makeLitTex(tex, 0.08f, 16f));
    }

    private Material makeLitTex(Texture2D tex, float spec, float shininess) {
        Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        m.setTexture("DiffuseMap", tex);
        m.setBoolean("UseMaterialColors", true);
        m.setColor("Diffuse", ColorRGBA.White);
        m.setColor("Specular", ColorRGBA.White.mult(spec));
        m.setFloat("Shininess", shininess);
        applyRenderFlags(m);
        return m;
    }

    private void applyRenderFlags(Material m) {
        m.getAdditionalRenderState().setFaceCullMode(culling ? RenderState.FaceCullMode.Back : RenderState.FaceCullMode.Off);
        m.getAdditionalRenderState().setWireframe(wireframe);
    }

    public void buildMeshes() {
        node.detachAllChildren();
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    chunk.buildMesh(assetManager, palette);
                    node.attachChild(chunk.getNode());
                }
            }
        }
    }

    public void buildPhysics(PhysicsSpace space) {
        // Build per-chunk static rigid bodies instead of a single world body
        if (space == null) return;
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    chunk.updatePhysics(space);
                }
            }
        }
    }

    public Optional<Hit> pickFirstSolid(Camera cam, float maxDistance) {
        Vector3f origin = cam.getLocation();
        Vector3f dir = cam.getDirection().normalize();

        int x = (int) Math.floor(origin.x);
        int y = (int) Math.floor(origin.y);
        int z = (int) Math.floor(origin.z);

        float tMaxX, tMaxY, tMaxZ;
        float tDeltaX, tDeltaY, tDeltaZ;
        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        float nextVoxelBoundaryX = x + (stepX > 0 ? 1 : 0);
        float nextVoxelBoundaryY = y + (stepY > 0 ? 1 : 0);
        float nextVoxelBoundaryZ = z + (stepZ > 0 ? 1 : 0);

        tMaxX = (dir.x != 0) ? (nextVoxelBoundaryX - origin.x) / dir.x : Float.POSITIVE_INFINITY;
        tMaxY = (dir.y != 0) ? (nextVoxelBoundaryY - origin.y) / dir.y : Float.POSITIVE_INFINITY;
        tMaxZ = (dir.z != 0) ? (nextVoxelBoundaryZ - origin.z) / dir.z : Float.POSITIVE_INFINITY;

        tDeltaX = (dir.x != 0) ? stepX / dir.x : Float.POSITIVE_INFINITY;
        tDeltaY = (dir.y != 0) ? stepY / dir.y : Float.POSITIVE_INFINITY;
        tDeltaZ = (dir.z != 0) ? stepZ / dir.z : Float.POSITIVE_INFINITY;

        float t = 0f;
        // starting inside a solid block
        if (inBounds(x,y,z) && isSolid(x,y,z)) {
            return Optional.of(new Hit(new Vector3i(x,y,z), new Vector3f(0,0,0), 0f));
        }

        Vector3f lastNormal = new Vector3f(0,0,0);

        while (t <= maxDistance) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX; t = tMaxX; tMaxX += tDeltaX;
                    lastNormal.set(-stepX, 0, 0);
                } else {
                    z += stepZ; t = tMaxZ; tMaxZ += tDeltaZ;
                    lastNormal.set(0, 0, -stepZ);
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY; t = tMaxY; tMaxY += tDeltaY;
                    lastNormal.set(0, -stepY, 0);
                } else {
                    z += stepZ; t = tMaxZ; tMaxZ += tDeltaZ;
                    lastNormal.set(0, 0, -stepZ);
                }
            }

            if (!inBounds(x,y,z)) {
                if (t > maxDistance) break;
                continue;
            }
            if (isSolid(x,y,z)) {
                return Optional.of(new Hit(new Vector3i(x,y,z), lastNormal.clone(), t));
            }
        }
        return Optional.empty();
    }

    public boolean isSolid(int x, int y, int z) {
        if (!inBounds(x,y,z)) return false;
        return palette.get(getBlock(x, y, z)).isSolid();
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    public void setLit(boolean lit) {
        if (this.lit == lit) return;
        this.lit = lit;
        for (var e : geoms.entrySet()) {
            Geometry g = e.getValue();
            var oldMat = g.getMaterial();
            com.jme3.texture.Texture tex = oldMat.getTextureParam("DiffuseMap") != null
                    ? oldMat.getTextureParam("DiffuseMap").getTextureValue()
                    : (oldMat.getTextureParam("ColorMap") != null ? oldMat.getTextureParam("ColorMap").getTextureValue() : null);
            Material newMat;
            if (this.lit) {
                newMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                if (tex != null) newMat.setTexture("DiffuseMap", tex);
                newMat.setBoolean("UseMaterialColors", true);
                newMat.setColor("Diffuse", ColorRGBA.White);
                newMat.setColor("Specular", ColorRGBA.White.mult(0.08f));
                newMat.setFloat("Shininess", 16f);
            } else {
                newMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                if (tex != null) newMat.setTexture("ColorMap", tex);
            }
            applyRenderFlags(newMat);
            g.setMaterial(newMat);
        }
    }

    public void setWireframe(boolean wireframe) {
        if (this.wireframe == wireframe) return;
        this.wireframe = wireframe;

        for (Geometry g : geoms.values()) applyRenderFlags(g.getMaterial());
    }

    public void setCulling(boolean culling) {
        if (this.culling == culling) return;
        this.culling = culling;
        for (Geometry g : geoms.values()) applyRenderFlags(g.getMaterial());
    }

    public boolean isLit() { return lit; }
    public boolean isWireframe() { return wireframe; }
    public boolean isCulling() { return culling; }

    public void toggleRenderDebug() {
        System.out.println("Toggled render debug");
        setLit(!isLit());
        setWireframe(!isWireframe());
        setCulling(!isCulling());
    }

    public int getGroundHeight() { return groundHeight; }

    public VoxelPalette getPalette() {
        return palette;
    }

    /**
     * Rebuilds meshes only for dirty chunks. Call this once per frame in your update loop.
     */
    public void rebuildDirtyChunks(PhysicsSpace physicsSpace) {
        int rebuilt = 0;
        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cy = 0; cy < chunkCountY; cy++) {
                for (int cz = 0; cz < chunkCountZ; cz++) {
                    Chunk chunk = chunks[cx][cy][cz];
                    if (chunk.isDirty()) {
                        System.out.println("Rebuilding chunk: " + cx + "," + cy + "," + cz);
                        chunk.buildMesh(assetManager, palette);
                        chunk.updatePhysics(physicsSpace);
                        chunk.clearDirty();
                        rebuilt++;
                    }
                }
            }
        }
        if (rebuilt > 0) System.out.println("Chunks rebuilt this frame: " + rebuilt);
        if (rebuilt > 0 && physicsSpace != null) {
            physicsSpace.update(0); // Force physics space to process changes
            System.out.println("Physics space forced update after chunk physics changes.");
        }
    }

    /**
     * Clears the dirty flag on all chunks. Call after initial buildMeshes().
     */
    public void clearAllDirtyFlags() {
        for (int cx = 0; cx < chunkCountX; cx++)
            for (int cy = 0; cy < chunkCountY; cy++)
                for (int cz = 0; cz < chunkCountZ; cz++)
                    chunks[cx][cy][cz].clearDirty();
    }

    public int getSizeX() {
        return  sizeX;
    }

    public int getSizeY() {
        return  sizeY;
    }

    public int getSizeZ() {
        return  sizeZ;
    }

    public void updateTickableBlocks(Vector3f center, float tpf, PhysicsSpace physicsSpace) {
        if (physicsSpace == null) return;

        boolean worldChanged = false;

        // Converte a posição central para coordenadas de bloco
        int px = (int) Math.floor(center.x);
        int py = (int) Math.floor(center.y);
        int pz = (int) Math.floor(center.z);

        // Limites da área a processar
        int minX = Math.max(0, px - TICKET_RADIUS);
        int maxX = Math.min(sizeX - 1, px + TICKET_RADIUS);

        int minY = Math.max(1, py - TICKET_RADIUS);
        int maxY = Math.min(sizeY - 1, py + TICKET_RADIUS);

        int minZ = Math.max(0, pz - TICKET_RADIUS);
        int maxZ = Math.min(sizeZ - 1, pz + TICKET_RADIUS);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {

                    byte currentId = getBlock(x, y, z);
                    VoxelBlockType type = palette.get(currentId);

                    if (type.isTickable()) {
                        if (type.onTick(x, y, z, this, tpf)) {
                            worldChanged = true;
                        }
                    }
                }
            }
        }

        if (worldChanged) {
            rebuildDirtyChunks(physicsSpace);
        }
    }

    public FurnaceState getFurnaceState(int x, int y, int z) {
        Vector3i key = new Vector3i(x, y, z);
        if (getBlock(x, y, z) != VoxelPalette.FURNACE_ID) {
            furnaceStates.remove(key);
            return null;
        }
        return furnaceStates.computeIfAbsent(key, k -> new FurnaceState());
    }

    // método para remover estado
    public FurnaceState removeFurnaceState(int x, int y, int z) {
        return furnaceStates.remove(new Vector3i(x, y, z));
    }

    public void updateAllFurnaces(float tpf, PhysicsSpace physicsSpace) {
        // Usa um HashSet das keys para evitar ConcurrentModificationException
        for (Vector3i key : new java.util.HashSet<>(furnaceStates.keySet())) {
            FurnaceState state = furnaceStates.get(key);

            if (state != null) {
                state.updateMelt(tpf);
            }
        }
    }


    // simple int3
    public static class Vector3i {
        public final int x, y, z;
        public Vector3i(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }

        public Vector3i(Vector3f vec3f) {
            this.x = (int) vec3f.x;
            this.y = (int) vec3f.y;
            this.z = (int) vec3f.z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vector3i vector3i = (Vector3i) o;
            return x == vector3i.x && y == vector3i.y && z == vector3i.z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    public static class SimpleNoise {

        private final long seed;

        public SimpleNoise(long seed) {
            this.seed = seed;
        }

        public float noise(float x, float z) {
            // Coordenadas de grid
            int x0 = (int) Math.floor(x);
            int z0 = (int) Math.floor(z);
            int x1 = x0 + 1;
            int z1 = z0 + 1;

            // Interpoladores (entre 0 e 1)
            float sx = smooth(x - x0);
            float sz = smooth(z - z0);

            // Valores de noise nos 4 cantos
            float n00 = rawNoise(x0, z0);
            float n10 = rawNoise(x1, z0);
            float n01 = rawNoise(x0, z1);
            float n11 = rawNoise(x1, z1);

            // Interpolação horizontal
            float ix0 = lerp(n00, n10, sx);
            float ix1 = lerp(n01, n11, sx);

            // Interpolação vertical final
            return lerp(ix0, ix1, sz);
        }


        // Noise bruto (0–1 → -1..1)
        private float rawNoise(int x, int z) {
            return (hash(x, z) & 0xFFFF) / 32768f - 1f;
        }

        // Interpolação linear
        private float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        // Função smoothstep (suaviza transições)
        private float smooth(float t) {
            return t * t * (3 - 2 * t);
        }


        private int hash(int x, int z) {
            long h = seed;

            h ^= x * 374761393L;
            h ^= z * 668265263L;

            h = (h ^ (h >> 13)) * 1274126177L;
            return (int) (h ^ (h >> 16));
        }
    }

    private void generateTree(int x, int y, int z) {

        int trunkHeight = 4 + (int) (Math.random() * 3);

        // Tronco
        for (int i = 0; i < trunkHeight; i++) {
            safeSetBlock(x, y + i, z, WOOD);
        }

        int topY = y + trunkHeight;

        // Folhas
        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                for (int ly = -1; ly <= 2; ly++) {

                    float dist = Math.abs(lx) + Math.abs(lz) + Math.abs(ly);
                    if (dist <= 3) {
                        safeSetBlock(x + lx, topY + ly, z + lz, LEAVES);
                    }
                }
            }
        }
    }

    private void generateHotTree(int x, int y, int z) {

        int trunkHeight = 4 + (int) (Math.random() * 3);

        // Tronco
        for (int i = 0; i < trunkHeight; i++) {
            safeSetBlock(x, y + i, z, WOOD);
        }
    }

    private void safeSetBlock(int x, int y, int z, byte id) {
        if (x < 0 || x >= sizeX) return;
        if (y < 0 || y >= sizeY) return;
        if (z < 0 || z >= sizeZ) return;

        setBlock(x, y, z, id);
    }
}
