# COBALT
![Cobalt logo image](https://cdn.modrinth.com/data/cached_images/c6eb4fb047b363e19e5f90f69d4713bed313f3bb.png)
## ⚠️ WARNING: Early Development Stage
**Cobalt is currently in its early stages of development. Users may encounter bugs, unexpected behavior, or compatibility issues with other mods. Performance gains may vary significantly depending on system specifications and world complexity. Use at your own risk. If you encounter any issues, please report them [here](https://github.com/Kubik-Modder/cobalt-fabric/issues).**

## Overview
Cobalt is an innovative, experimental Minecraft mod designed to dramatically enhance game performance through intelligent chunk rendering optimization. By leveraging advanced algorithms to dynamically manage chunk loading, Cobalt aims to significantly boost frame rates and reduce GPU load, particularly in complex worlds or when using high render distances.

## Key Features

### 1. Dynamic Chunk Rendering
- Selectively renders chunks based on the player's direct line of sight
- Utilizes sophisticated calculations to determine visibility and rendering priority

### 2. Smart Chunk Management
- Maintains a small buffer of always-rendered chunks around the player for seamless movement
- Implements a caching system to temporarily retain recently viewed chunks, ensuring smooth transitions

### 3. Compatibility
- Special compatibility layer for integration with Sodium
- May not work with other mods that alter chunk rendering

## Performance Impact

### Potential Benefits
- Substantial FPS increases, especially in demanding scenarios or with high render distances
- Significantly reduced GPU workload and memory usage
- Improved overall game responsiveness and stability

### Considerations
- Slight increase in CPU utilization due to chunk visibility calculations
- Initial chunk loading may experience brief delays as the system optimizes rendering

## Benchmark Comparison
**Without Cobalt:**

![Without mod (250 FPS on average)](https://cdn.modrinth.com/data/cached_images/dc919a8c59a5b8c3e4a50f48e1f7ae538990d50e.png)

Average FPS: **250**

**With Cobalt:**

![With mod (350 FPS on average)](https://cdn.modrinth.com/data/cached_images/bbeeb6de04e2f0f6393c0f206cb3f6ec04f85373.png)

Average FPS: **350**

*Note: Performance gains may vary based on system specifications and world complexity.*

## Technical Details
- **Implementation:** Client-side only, no server-side installation required
- **Compatibility:** Works with both vanilla and modded servers
- **Framework:** Built on Fabric mod loader for 1.20.1, 1.21
- **Language:** Developed in Java

## Ideal Use Cases
- Players seeking to increase render distances without sacrificing performance
- Users with complex builds or resource-intensive worlds
- Minecraft enthusiasts looking to optimize their gameplay experience on lower-end hardware

Cobalt represents a cutting-edge approach to Minecraft optimization, offering a unique solution for players seeking to enhance their gameplay experience without compromising on visual quality or view distance.
