package jogo.gameobject.npc;

import com.jme3.bullet.control.RigidBodyControl;
import jogo.appstate.NPCAppState;
import jogo.gameobject.character.Character;
import jogo.framework.math.Vec3;
import jogo.gameobject.item.Item;
import jogo.gameobject.item.Tool;

public abstract class NPC extends Character {
    public NPCAppState appStateHook;

    public NPC(String name) {
        super(name);
    }

    // Atualização de IA (cada NPC implementa a sua)
    public abstract void updateAI(float tpf);

    // Move logicamente o NPC
    public void move(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }

    // Distância lógica
    public float distanceTo(Vec3 other) {
        return this.position.distance(other);
    }

    public void setPhysicsControl(RigidBodyControl rbc) {

    }

    public RigidBodyControl getPhysicsControl() {

        return null;
    }

    public void takeDamage(Item heldItem) {
        // Dano base (mão)
        float baseDamage = 3.0f; // 3 de dano base da mão
        float multiplier = 1.0f;
        String source = "Mão";

        if (heldItem instanceof Tool tool) {
            multiplier = tool.getAttackMultiplier();
            source = tool.getName();
        }

        int finalDamage = (int) (baseDamage * multiplier);

        int oldHealth = getHealth(); // [NOVO] Capturar vida antiga
        super.takeDamage(finalDamage); // Chama o takeDamage base do Character

        int newHealth = getHealth(); // [NOVO] Capturar vida nova

        // [NOVO] Lógica de MORTE
        if (newHealth <= 0 && oldHealth > 0) {
            System.out.println(getName() + " morreu.");
            if (appStateHook != null) {
                appStateHook.removeNPC(this); // [NOVO] Notificar o AppState para remover o modelo/AI
            }
        } else {
            // Lógica de feedback normal
            System.out.println(getName() + " sofreu " + finalDamage + " de dano de " + source + ". Vida atual: " + newHealth);
        }
    }
}