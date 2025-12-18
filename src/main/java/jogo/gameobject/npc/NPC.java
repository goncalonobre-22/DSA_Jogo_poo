package jogo.gameobject.npc;

import com.jme3.bullet.control.RigidBodyControl;
import jogo.appstate.NPCAppState;
import jogo.gameobject.character.Character;
import jogo.framework.math.Vec3;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.Tool;

public abstract class NPC extends Character {
    public NPCAppState appStateHook;

    protected float verticalVelocity = 0;
    protected static final float GRAVITY = 24.0f;

    public NPC(String name) {
        super(name);
    }

    // Atualização de IA
    public abstract void updateAI(float tpf);

    // Move logicamente o NPC
    public void move(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }


    public void takeDamage(Item heldItem) {
        // Dano base (mão)
        float baseDamage = 3.0f;
        float multiplier = 1.0f;
        String source = "Mão";

        if (heldItem instanceof Tool tool) {
            multiplier = tool.getAttackMultiplier();
            source = tool.getName();
        }

        int finalDamage = (int) (baseDamage * multiplier);

        int oldHealth = getHealth();
        super.takeDamage(finalDamage);

        int newHealth = getHealth();

        // Lógica de morte
        if (newHealth <= 0 && oldHealth > 0) {
            System.out.println(getName() + " morreu.");
            if (appStateHook != null) {
                appStateHook.removeNPC(this); // [NOVO] Notificar o AppState para remover o modelo/AI
            }
        } else {
            System.out.println(getName() + " sofreu " + finalDamage + " de dano de " + source + ". Vida atual: " + newHealth);
        }
    }
}