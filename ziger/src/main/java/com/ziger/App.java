package com.ziger;


import java.io.DataOutputStream;

import arc.Events;
import arc.util.Timer;
import arc.util.io.ReusableByteOutStream;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.type.Item;
import mindustry.type.Liquid;
/**
 * Hello world!
 *
 */
public class App extends Plugin
{
    private ReusableByteOutStream syncStream;
    private DataOutputStream dataStream;



    @Override
    public void init() {
        this.syncStream = new ReusableByteOutStream();
        
        Timer.schedule(() -> {
            Groups.build.each((build) -> add(build));
        }, 0.0f, 0.2f);
        
    }

    private void add(Building build) {
        for (Item item : Vars.content.items()) {
            if (build.block.consumesItem(item)) {
                if (build.items.get(item) < build.block.itemCapacity) {
                    build.items.add(item, build.block.itemCapacity);
                    sendBlockSnapshot(build);
                
                }
            }
        }
        for (Liquid liquid : Vars.content.liquids()) {
            if (build.block.consumesLiquid(liquid)) {
                if (build.liquids.get(liquid) < build.block.liquidCapacity) {
                    build.liquids.add(liquid, build.block.itemCapacity);
                    sendBlockSnapshot(build);
                }
            }
        }
    }

    private void sendBlockSnapshot(Building build) {
        this.dataStream = new DataOutputStream(this.syncStream);
        dataStream.writeInt(build.pos());
        dataStream.writeShort(build.block.id);
        dataStream.flush();
        build.writeAll(Writes.get(dataStream));
        Call.blockSnapshot((short)1, syncStream.toByteArray());
        this.syncStream.reset();
        this.dataStream.close();
    }
}
