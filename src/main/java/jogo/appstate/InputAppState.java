package jogo.appstate;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class InputAppState extends BaseAppState implements ActionListener, AnalogListener {

    private boolean forward, backward, left, right;
    private boolean sprint;
    private volatile boolean jumpRequested;
    private volatile boolean breakRequested;
    private volatile boolean toggleShadingRequested;
    private volatile boolean respawnRequested;
    private volatile boolean interactRequested;
    private float mouseDX, mouseDY;
    private boolean mouseCaptured = true;
    // Adicionado do  inventário 1
    private volatile boolean placeRequested;
    private volatile boolean toggleInventoryRequested;
    private int scrollDelta = 0;
    private volatile boolean inventoryLeftRequested;
    private volatile boolean inventoryRightRequested;
    private volatile boolean inventoryUpRequested;
    private volatile boolean inventoryDownRequested;
    private volatile int hotbarNumberPressed = 0;
    private volatile boolean takeRequested;
    private volatile boolean putRequested;
    private volatile boolean craftMenuRequested;
    private volatile boolean exitCraftRequested;
    private volatile boolean craftArrowUp;
    private volatile boolean craftArrowDown;
    private volatile boolean craftArrowLeft;
    private volatile boolean craftArrowRight;



    @Override
    protected void initialize(Application app) {
        var im = app.getInputManager();
        // Movement keys
        im.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        im.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        im.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        im.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        im.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        // Mouse look
        im.addMapping("MouseX+", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        im.addMapping("MouseX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        im.addMapping("MouseY+", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        im.addMapping("MouseY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        // Toggle capture (use TAB, ESC exits app by default)
        im.addMapping("ToggleMouse", new KeyTrigger(KeyInput.KEY_TAB));
        // Break voxel (left mouse)
        im.addMapping("Break", new MouseButtonTrigger(com.jme3.input.MouseInput.BUTTON_LEFT));
        // Toggle shading (L)
        im.addMapping("ToggleShading", new KeyTrigger(KeyInput.KEY_L));
        // Respawn (R)
        im.addMapping("Respawn", new KeyTrigger(KeyInput.KEY_R));
        // Interact (E)
        im.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));

        // Mouse wheel
        im.addMapping("MouseWheelUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        im.addMapping("MouseWheelDown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        // Place voxel (right mouse)
        im.addMapping("Place", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        // Toggle Inventory (I)
        im.addMapping("ToggleInventory", new KeyTrigger(KeyInput.KEY_TAB));

        im.addMapping("InvLeft",  new KeyTrigger(KeyInput.KEY_LEFT));
        im.addMapping("InvRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        im.addMapping("InvUp",    new KeyTrigger(KeyInput.KEY_UP));
        im.addMapping("InvDown",  new KeyTrigger(KeyInput.KEY_DOWN));

        im.addMapping("Hotbar1", new KeyTrigger(KeyInput.KEY_1));
        im.addMapping("Hotbar2", new KeyTrigger(KeyInput.KEY_2));
        im.addMapping("Hotbar3", new KeyTrigger(KeyInput.KEY_3));
        im.addMapping("Hotbar4", new KeyTrigger(KeyInput.KEY_4));
        im.addMapping("Hotbar5", new KeyTrigger(KeyInput.KEY_5));
        im.addMapping("Hotbar6", new KeyTrigger(KeyInput.KEY_6));
        im.addMapping("Hotbar7", new KeyTrigger(KeyInput.KEY_7));
        im.addMapping("Hotbar8", new KeyTrigger(KeyInput.KEY_8));
        im.addMapping("Hotbar9", new KeyTrigger(KeyInput.KEY_9));
        im.addMapping("Hotbar10", new KeyTrigger(KeyInput.KEY_0));

        im.addMapping("Take", new KeyTrigger(KeyInput.KEY_T));
        im.addMapping("Put", new KeyTrigger(KeyInput.KEY_P));
        im.addMapping("CraftMenu", new KeyTrigger(KeyInput.KEY_RETURN)); // ENTER
        im.addMapping("ExitCraft", new KeyTrigger(KeyInput.KEY_LMENU)); // ALT
        im.addMapping("CraftUp", new KeyTrigger(KeyInput.KEY_UP));
        im.addMapping("CraftDown", new KeyTrigger(KeyInput.KEY_DOWN));
        im.addMapping("CraftLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        im.addMapping("CraftRight", new KeyTrigger(KeyInput.KEY_RIGHT));

        im.addListener(this, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Jump", "Sprint", "ToggleMouse", "Break", "ToggleShading", "Respawn", "Interact");
        im.addListener(this, "MouseX+", "MouseX-", "MouseY+", "MouseY-");
        // Adicionado do inventário pt.2
        im.addListener(this, "MoveForward", "MoveBackward", "MoveLeft", "MoveRight", "Jump", "Sprint",
                "ToggleMouse", "Break", "Place", "ToggleShading", "Respawn", "Interact", "ToggleInventory");
        im.addListener(this, "MouseX+", "MouseX-", "MouseY+", "MouseY-", "MouseWheelUp", "MouseWheelDown");
        im.addListener(this, "InvLeft", "InvRight", "InvUp", "InvDown");
        im.addListener(this, "Hotbar1","Hotbar2","Hotbar3","Hotbar4","Hotbar5","Hotbar6","Hotbar7","Hotbar8","Hotbar9", "Hotbar10");
        im.addListener(this, "Take", "Put", "CraftMenu", "ExitCraft",
                "CraftUp", "CraftDown", "CraftLeft", "CraftRight");
    }

    @Override
    protected void cleanup(Application app) {
        var im = app.getInputManager();
        im.deleteMapping("MoveForward");
        im.deleteMapping("MoveBackward");
        im.deleteMapping("MoveLeft");
        im.deleteMapping("MoveRight");
        im.deleteMapping("Jump");
        im.deleteMapping("Sprint");
        im.deleteMapping("MouseX+");
        im.deleteMapping("MouseX-");
        im.deleteMapping("MouseY+");
        im.deleteMapping("MouseY-");
        im.deleteMapping("ToggleMouse");
        im.deleteMapping("Break");
        im.deleteMapping("ToggleShading");
        im.deleteMapping("Respawn");
        im.deleteMapping("Interact");
        im.removeListener(this);


        // Adicionado do inventário pt.3
        im.deleteMapping("MouseWheelUp");
        im.deleteMapping("MouseWheelDown");
        im.deleteMapping("Place");
        im.deleteMapping("ToggleInventory");

        im.deleteMapping("Take");
        im.deleteMapping("Put");
        im.deleteMapping("CraftMenu");
        im.deleteMapping("ExitCraft");
        im.deleteMapping("CraftUp");
        im.deleteMapping("CraftDown");
        im.deleteMapping("CraftLeft");
        im.deleteMapping("CraftRight");
    }

    @Override
    protected void onEnable() {
        setMouseCaptured(true);
    }

    @Override
    protected void onDisable() { }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "MoveForward" -> forward = isPressed;
            case "MoveBackward" -> backward = isPressed;
            case "MoveLeft" -> left = isPressed;
            case "MoveRight" -> right = isPressed;
            case "Sprint" -> sprint = isPressed;
            case "Jump" -> {
                if (isPressed) jumpRequested = true;
            }
            case "ToggleMouse" -> {
                if (isPressed) setMouseCaptured(!mouseCaptured);
            }
            case "Break" -> {
                if (isPressed && mouseCaptured) breakRequested = true;
            }
            case "ToggleShading" -> {
                if (isPressed) toggleShadingRequested = true;
            }
            case "Respawn" -> {
                if (isPressed) respawnRequested = true;
            }
            case "Interact" -> {
                if (isPressed && mouseCaptured) interactRequested = true;
            }

            // Adicionado do inventário pt.4
            case "Place" -> {
                if (isPressed && mouseCaptured) placeRequested = true;
            }
            case "ToggleInventory" -> {
                if (isPressed) toggleInventoryRequested = true;
            }

            case "InvLeft"  -> {
                if (isPressed) inventoryLeftRequested  = true;
            }
            case "InvRight" -> {
                if (isPressed) inventoryRightRequested = true;
            }
            case "InvUp"    -> {
                if (isPressed) inventoryUpRequested    = true;
            }
            case "InvDown"  -> {
                if (isPressed) inventoryDownRequested  = true;
            }
            case "Hotbar1" -> { if (isPressed) hotbarNumberPressed = 1; }
            case "Hotbar2" -> { if (isPressed) hotbarNumberPressed = 2; }
            case "Hotbar3" -> { if (isPressed) hotbarNumberPressed = 3; }
            case "Hotbar4" -> { if (isPressed) hotbarNumberPressed = 4; }
            case "Hotbar5" -> { if (isPressed) hotbarNumberPressed = 5; }
            case "Hotbar6" -> { if (isPressed) hotbarNumberPressed = 6; }
            case "Hotbar7" -> { if (isPressed) hotbarNumberPressed = 7; }
            case "Hotbar8" -> { if (isPressed) hotbarNumberPressed = 8; }
            case "Hotbar9" -> { if (isPressed) hotbarNumberPressed = 9; }
            case  "Hotbar10" -> { if (isPressed) hotbarNumberPressed = 10; }

            case "Take" -> {
                if (isPressed) takeRequested = true;
            }
            case "Put" -> {
                if (isPressed) putRequested = true;
            }
            case "CraftMenu" -> {
                if (isPressed) craftMenuRequested = true;
            }
            case "ExitCraft" -> {
                if (isPressed) exitCraftRequested = true;
            }
            case "CraftUp" -> {
                if (isPressed) craftArrowUp = true;
            }
            case "CraftDown" -> {
                if (isPressed) craftArrowDown = true;
            }
            case "CraftLeft" -> {
                if (isPressed) craftArrowLeft = true;
            }
            case "CraftRight" -> {
                if (isPressed) craftArrowRight = true;
            }

        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (!mouseCaptured) return;
        switch (name) {
            case "MouseX+" -> mouseDX += value;
            case "MouseX-" -> mouseDX -= value;
            case "MouseY+" -> mouseDY += value;
            case "MouseY-" -> mouseDY -= value;

            //Adicionado do inventário pt.5
            case "MouseWheelUp" -> scrollDelta = 1;
            case "MouseWheelDown" -> scrollDelta = -1;
        }
    }

    public Vector3f getMovementXZ() {
        float fb = (forward ? 1f : 0f) + (backward ? -1f : 0f);
        float lr = (right ? 1f : 0f) + (left ? -1f : 0f);
        return new Vector3f(lr, 0f, -fb); // -fb so forward maps to -Z in JME default
    }

    public boolean isSprinting() {
        return sprint;
    }

    public boolean consumeJumpRequested() {
        boolean jr = jumpRequested;
        jumpRequested = false;
        return jr;
    }

    public boolean consumeBreakRequested() {
        boolean r = breakRequested;
        breakRequested = false;
        return r;
    }

    public boolean consumeToggleShadingRequested() {
        boolean r = toggleShadingRequested;
        toggleShadingRequested = false;
        return r;
    }

    public boolean consumeRespawnRequested() {
        boolean r = respawnRequested;
        respawnRequested = false;
        return r;
    }

    public boolean consumeInteractRequested() {
        boolean r = interactRequested;
        interactRequested = false;
        return r;
    }

    public Vector2f consumeMouseDelta() {
        Vector2f d = new Vector2f(mouseDX, mouseDY);
        mouseDX = 0f;
        mouseDY = 0f;
        return d;
    }

    public void setMouseCaptured(boolean captured) {
        this.mouseCaptured = captured;
        var im = getApplication().getInputManager();
        im.setCursorVisible(!captured);
        // Clear accumulated deltas when switching state
        mouseDX = 0f;
        mouseDY = 0f;
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }

    public boolean consumePlaceRequested() {
        boolean r = placeRequested;
        placeRequested = false;
        return r;
    }

    public boolean consumeToggleInventoryRequested() {
        boolean r = toggleInventoryRequested;
        toggleInventoryRequested = false;
        return r;
    }

    public int consumeScrollDelta() {
        int d = scrollDelta;
        scrollDelta = 0;
        return d;
    }

    public boolean consumeInventoryLeft() {
        boolean r = inventoryLeftRequested;
        inventoryLeftRequested = false;
        return r;
    }

    public boolean consumeInventoryRight() {
        boolean r = inventoryRightRequested;
        inventoryRightRequested = false;
        return r;
    }

    public boolean consumeInventoryUp() {
        boolean r = inventoryUpRequested;
        inventoryUpRequested = false;
        return r;
    }

    public boolean consumeInventoryDown() {
        boolean r = inventoryDownRequested;
        inventoryDownRequested = false;
        return r;
    }

    public int consumeHotbarNumber() {
        int r = hotbarNumberPressed;
        hotbarNumberPressed = 0;
        return r;
    }

    public boolean consumeTakeRequested() {
        boolean r = takeRequested;
        takeRequested = false;
        return r;
    }

    public boolean consumePutRequested() {
        boolean r = putRequested;
        putRequested = false;
        return r;
    }

    public boolean consumeCraftMenuRequested() {
        boolean r = craftMenuRequested;
        craftMenuRequested = false;
        return r;
    }

    public boolean consumeExitCraftRequested() {
        boolean r = exitCraftRequested;
        exitCraftRequested = false;
        return r;
    }

    public boolean consumeCraftArrowUp() {
        boolean r = craftArrowUp;
        craftArrowUp = false;
        return r;
    }

    public boolean consumeCraftArrowDown() {
        boolean r = craftArrowDown;
        craftArrowDown = false;
        return r;
    }

    public boolean consumeCraftArrowLeft() {
        boolean r = craftArrowLeft;
        craftArrowLeft = false;
        return r;
    }

    public boolean consumeCraftArrowRight() {
        boolean r = craftArrowRight;
        craftArrowRight = false;
        return r;
    }


}
